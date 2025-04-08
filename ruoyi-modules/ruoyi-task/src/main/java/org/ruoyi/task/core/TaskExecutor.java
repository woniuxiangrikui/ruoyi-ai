package org.ruoyi.task.core;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ruoyi.common.chat.entity.chat.*;
import org.ruoyi.common.chat.openai.OpenAiClient;
import org.ruoyi.common.chat.openai.plugin.PluginAbstract;
import org.ruoyi.common.chat.openai.plugin.PluginParam;
import org.ruoyi.task.domain.Task;
import org.ruoyi.task.domain.TaskPlan;
import org.ruoyi.task.domain.TaskExecutionResult;
import org.ruoyi.task.domain.TaskProgressEvent;
import org.ruoyi.task.listener.TaskExecutionListener;
import org.springframework.stereotype.Component;
import cn.hutool.json.JSONUtil;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

/**
 * 任务执行器
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class TaskExecutor {
    private final OpenAiClient openAiClient;
    private final PluginRegistryService pluginRegistry;
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    /**
     * 执行任务计划中的所有任务
     *
     * @param taskPlan 任务计划
     * @param userInput 用户输入
     * @return 执行结果
     */
    public TaskExecutionResult executeTasks(TaskPlan taskPlan, String userInput) {
        return executeTasks(taskPlan, userInput, null);
    }
    
    /**
     * 执行任务计划中的所有任务，并向监听器报告进度
     *
     * @param taskPlan 任务计划
     * @param userInput 用户输入
     * @param listener 任务执行监听器
     * @return 执行结果
     */
    public TaskExecutionResult executeTasks(TaskPlan taskPlan, String userInput, TaskExecutionListener listener) {
        try {
            // 通知监听器任务计划生成
            if (listener != null) {
                TaskProgressEvent planEvent = new TaskProgressEvent(
                    TaskProgressEvent.EventType.PLAN_GENERATED,
                    taskPlan.getAnalysis(),
                    taskPlan.getTasks(),
                    null, 
                    null,
                    System.currentTimeMillis()
                );
                listener.onTaskPlanGenerated(planEvent);
            }
            
            List<Task> tasks = taskPlan.getTasks();
            if (tasks == null || tasks.isEmpty()) {
                log.warn("任务计划中没有任务需要执行");
                TaskExecutionResult emptyResult = new TaskExecutionResult(true, "没有任务需要执行", null);
                
                // 通知监听器所有任务完成
                if (listener != null) {
                    TaskProgressEvent completedEvent = new TaskProgressEvent(
                        TaskProgressEvent.EventType.ALL_COMPLETED,
                        taskPlan.getAnalysis(),
                        tasks,
                        null, 
                        "没有任务需要执行",
                        System.currentTimeMillis()
                    );
                    listener.onAllTasksCompleted(completedEvent);
                }
                
                return emptyResult;
            }
            
            log.info("开始执行任务计划，共 {} 个任务", tasks.size());
            List<Object> results = new ArrayList<>();
            
            // 顺序执行所有任务
            for (Task task : tasks) {
                try {
                    String taskId = task.getId();
                    log.info("开始执行任务: {}, 插件: {}, 功能: {}", 
                              taskId, task.getPluginName(), task.getFunction());
                    
                    // 通知监听器任务开始
                    if (listener != null) {
                        TaskProgressEvent startEvent = new TaskProgressEvent(
                            TaskProgressEvent.EventType.TASK_STARTED,
                            taskPlan.getAnalysis(),
                            tasks,
                            taskId, 
                            null,
                            System.currentTimeMillis()
                        );
                        listener.onTaskStarted(startEvent);
                    }
                    
                    // 获取插件定义
                    PluginDefinition pluginDef = pluginRegistry.getPlugin(task.getPluginName());
                    if (pluginDef == null) {
                        String error = "未找到插件: " + task.getPluginName();
                        log.error(error);
                        
                        // 通知监听器任务失败
                        if (listener != null) {
                            TaskProgressEvent failedEvent = new TaskProgressEvent(
                                TaskProgressEvent.EventType.TASK_FAILED,
                                taskPlan.getAnalysis(),
                                tasks,
                                taskId, 
                                error,
                                System.currentTimeMillis()
                            );
                            listener.onTaskFailed(failedEvent, new RuntimeException(error));
                        }
                        
                        continue;
                    }
                    
                    // 获取函数参数
                    Object paramObj = objectMapper.convertValue(
                        task.getParameters(), 
                        ((PluginAbstract<?>) pluginDef).getParamClass()
                    );
                    
                    // 执行插件功能
                    Method method = pluginDef.getClass().getMethod(task.getFunction(), ((PluginAbstract<?>) pluginDef).getParamClass());
                    Object result = method.invoke(pluginDef, paramObj);
                    results.add(result);
                    
                    log.info("任务 {} 执行成功", taskId);
                    
                    // 通知监听器任务完成
                    if (listener != null) {
                        TaskProgressEvent completedEvent = new TaskProgressEvent(
                            TaskProgressEvent.EventType.TASK_COMPLETED,
                            taskPlan.getAnalysis(),
                            tasks,
                            taskId, 
                            generateResultSummary(task, result),
                            System.currentTimeMillis()
                        );
                        listener.onTaskCompleted(completedEvent, result);
                    }
                    
                } catch (Exception e) {
                    String error = "执行任务 " + task.getId() + " 失败: " + e.getMessage();
                    log.error(error, e);
                    
                    // 通知监听器任务失败
                    if (listener != null) {
                        TaskProgressEvent failedEvent = new TaskProgressEvent(
                            TaskProgressEvent.EventType.TASK_FAILED,
                            taskPlan.getAnalysis(),
                            tasks,
                            task.getId(), 
                            error,
                            System.currentTimeMillis()
                        );
                        listener.onTaskFailed(failedEvent, e);
                    }
                }
            }
            
            // 生成总结果
            String resultSummary = generateFinalSummary(taskPlan, results);
            TaskExecutionResult executionResult = new TaskExecutionResult(true, resultSummary, results);
            
            // 通知监听器所有任务完成
            if (listener != null) {
                TaskProgressEvent allCompletedEvent = new TaskProgressEvent(
                    TaskProgressEvent.EventType.ALL_COMPLETED,
                    taskPlan.getAnalysis(),
                    tasks,
                    null, 
                    resultSummary,
                    System.currentTimeMillis()
                );
                listener.onAllTasksCompleted(allCompletedEvent);
            }
            
            return executionResult;
            
        } catch (Exception e) {
            String error = "执行任务计划失败: " + e.getMessage();
            log.error(error, e);
            
            // 通知监听器出现错误
            if (listener != null) {
                TaskProgressEvent failedEvent = new TaskProgressEvent(
                    TaskProgressEvent.EventType.TASK_FAILED,
                    taskPlan.getAnalysis(),
                    taskPlan.getTasks(),
                    null, 
                    error,
                    System.currentTimeMillis()
                );
                listener.onTaskFailed(failedEvent, e);
            }
            
            return new TaskExecutionResult(false, error, null);
        }
    }
    
    /**
     * 生成单个任务的结果摘要
     */
    private String generateResultSummary(Task task, Object result) {
        StringBuilder summary = new StringBuilder();
        
        // 根据不同的插件类型生成不同的摘要
        if (task.getPluginName().contains("Python")) {
            Map<String, Object> params = task.getParameters();
            summary.append("Python代码已生成，处理文件：").append(params.get("filePath"));
            
            if (params.containsKey("analysisType")) {
                summary.append("，分析类型：").append(params.get("analysisType"));
            }
            
            if (result instanceof String && ((String) result).length() > 100) {
                summary.append("。生成代码长度：").append(((String) result).length()).append("字符");
            }
        } else if (task.getPluginName().contains("命令执行")) {
            Map<String, Object> params = task.getParameters();
            summary.append("命令已执行：").append(params.get("command"));
            
            if (result instanceof String) {
                String cmdResult = (String) result;
                if (cmdResult.length() > 100) {
                    summary.append("。执行结果长度：").append(cmdResult.length()).append("字符");
                } else {
                    summary.append("。执行结果：").append(cmdResult);
                }
            }
        } else {
            summary.append("任务执行完成，ID：").append(task.getId());
            if (result != null) {
                summary.append("，结果类型：").append(result.getClass().getSimpleName());
            }
        }
        
        return summary.toString();
    }
    
    /**
     * 生成最终的结果摘要
     */
    private String generateFinalSummary(TaskPlan taskPlan, List<Object> results) {
        StringBuilder summary = new StringBuilder();
        summary.append(taskPlan.getAnalysis());
        
        if (results.isEmpty()) {
            summary.append(" 没有得到任何结果。");
            return summary.toString();
        }
        
        summary.append(" 执行了").append(results.size()).append("个任务。");
        
        // 检查是否有Python任务
        boolean hasPythonTask = taskPlan.getTasks().stream()
            .anyMatch(task -> task.getPluginName().contains("Python"));
        
        // 检查是否有命令执行任务
        boolean hasCmdTask = taskPlan.getTasks().stream()
            .anyMatch(task -> task.getPluginName().contains("命令执行"));
        
        if (hasPythonTask) {
            summary.append("生成了Python代码用于数据处理。");
        }
        
        if (hasCmdTask) {
            summary.append("执行了系统命令。");
        }
        
        return summary.toString();
    }
} 