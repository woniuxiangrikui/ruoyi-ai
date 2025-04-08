package org.ruoyi.task.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ruoyi.common.chat.openai.plugin.PluginAbstract;
import org.ruoyi.common.chat.openai.plugin.PluginDefinition;
import org.ruoyi.task.core.PluginRegistryService;
import org.ruoyi.task.core.TaskExecutor;
import org.ruoyi.task.core.TaskPlanGenerator;
import org.ruoyi.task.domain.TaskInfo;
import org.ruoyi.task.domain.TaskSession;
import org.ruoyi.task.mapper.TaskInfoMapper;
import org.ruoyi.task.mapper.TaskSessionMapper;
import org.ruoyi.task.plugin.PythonExecutePlugin;
import org.ruoyi.task.plugin.PythonGeneratePlugin;
import org.ruoyi.task.plugin.PythonGenerateReq;
import org.ruoyi.task.service.AiService;
import org.ruoyi.task.service.FileStorageService;
import org.ruoyi.task.service.ITaskService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 * 任务服务实现
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TaskServiceImpl implements ITaskService {
    
    private final AiService aiService;
    private final TaskPlanGenerator taskPlanGenerator;
    private final TaskExecutor taskExecutor;
    private final PluginRegistryService pluginRegistry;
    private final FileStorageService fileStorageService;
    private final TaskSessionMapper taskSessionMapper;
    private final TaskInfoMapper taskInfoMapper;
    private final ObjectMapper objectMapper;
    
    @PostConstruct
    public void init() {
        // 注册所有可用插件
        registerPlugins();
    }
    
    private void registerPlugins() {
        // 只保留Python相关插件
        PythonGeneratePlugin pythonGeneratePlugin = new PythonGeneratePlugin();
        PythonExecutePlugin pythonExecutePlugin = new PythonExecutePlugin();
        
        // 注册到注册表
        pluginRegistry.registerPlugin(pythonGeneratePlugin);
        pluginRegistry.registerPlugin(pythonExecutePlugin);
    }
    
    @Override
    @Transactional
    public String executeComplexTask(String userInput, String filePath) {
        try {
            log.info("执行复杂任务, 用户输入: {}, 文件路径: {}", userInput, filePath);
            
            // 创建会话
            String sessionId = UUID.randomUUID().toString();
            createSession(sessionId, userInput, filePath);
            
            // 生成任务计划
            String taskPlanJson = taskPlanGenerator.generateTaskPlan(userInput, filePath);
            log.info("生成任务计划: {}", taskPlanJson);
            
            // 解析任务计划
            Map<String, Object> taskPlan = objectMapper.readValue(taskPlanJson, new TypeReference<Map<String, Object>>() {});
            String analysis = (String) taskPlan.get("analysis");
            List<Map<String, Object>> tasks = (List<Map<String, Object>>) taskPlan.get("tasks");
            
            // 保存任务到数据库
            saveTasks(sessionId, tasks);
            
            // 依次执行任务
            for (TaskInfo task : taskInfoMapper.selectBySessionId(sessionId)) {
                executeTask(task);
            }
            
            // 生成结果摘要
            String summary = generateTaskSummary(sessionId);
            
            // 更新会话状态
            updateSessionStatus(sessionId, "COMPLETED", summary);
            
            return summary;
        } catch (Exception e) {
            log.error("执行复杂任务失败", e);
            return "执行任务失败: " + e.getMessage();
        }
    }
    
    @Override
    public SseEmitter executeComplexTaskAsync(String userInput, String filePath) {
        SseEmitter emitter = new SseEmitter(0L); // 无超时
        
        // 创建会话
        String sessionId = UUID.randomUUID().toString();
        createSession(sessionId, userInput, filePath);
        
        // 异步执行任务
        CompletableFuture.runAsync(() -> {
            try {
                // 发送任务开始事件
                emitter.send(SseEmitter.event()
                    .name("start")
                    .data(Map.of("message", "开始处理任务", "sessionId", sessionId)));
                
                // 生成任务计划
                emitter.send(SseEmitter.event()
                    .name("progress")
                    .data(Map.of("message", "正在生成任务计划...")));
                
                String taskPlanJson = taskPlanGenerator.generateTaskPlan(userInput, filePath);
                
                // 解析任务计划
                Map<String, Object> taskPlan = objectMapper.readValue(taskPlanJson, new TypeReference<Map<String, Object>>() {});
                String analysis = (String) taskPlan.get("analysis");
                List<Map<String, Object>> tasks = (List<Map<String, Object>>) taskPlan.get("tasks");
                
                // 保存任务到数据库
                saveTasks(sessionId, tasks);
                
                // 获取所有任务
                List<TaskInfo> taskList = taskInfoMapper.selectBySessionId(sessionId);
                
                // 依次执行任务
                for (int i = 0; i < taskList.size(); i++) {
                    TaskInfo task = taskList.get(i);
                    
                    // 发送任务开始事件
                    emitter.send(SseEmitter.event()
                        .name("task-start")
                        .data(Map.of(
                            "message", String.format("正在执行任务 %d/%d: %s", i+1, taskList.size(), task.getDescription()),
                            "taskId", task.getTaskId(),
                            "taskOrder", task.getTaskOrder()
                        )));
                    
                    // 执行任务
                    executeTask(task);
                    
                    // 发送任务完成事件
                    emitter.send(SseEmitter.event()
                        .name("task-complete")
                        .data(Map.of(
                            "message", String.format("任务 %d/%d 已完成", i+1, taskList.size()),
                            "taskId", task.getTaskId(),
                            "outputFile", task.getOutputFiles()
                        )));
                }
                
                // 生成结果摘要
                String summary = generateTaskSummary(sessionId);
                
                // 更新会话状态
                updateSessionStatus(sessionId, "COMPLETED", summary);
                
                // 发送任务完成事件
                emitter.send(SseEmitter.event()
                    .name("complete")
                    .data(Map.of(
                        "message", "所有任务已完成",
                        "sessionId", sessionId,
                        "summary", summary
                    )));
                
                emitter.complete();
            } catch (Exception e) {
                log.error("异步执行复杂任务失败", e);
                
                try {
                    // 更新会话状态
                    updateSessionStatus(sessionId, "FAILED", "执行失败: " + e.getMessage());
                    
                    // 发送错误事件
                    emitter.send(SseEmitter.event()
                        .name("error")
                        .data(Map.of(
                            "message", "任务执行失败: " + e.getMessage(),
                            "sessionId", sessionId
                        )));
                    
                    emitter.complete();
                } catch (IOException ex) {
                    emitter.completeWithError(ex);
                }
            }
        });
        
        return emitter;
    }
    
    @Override
    public String getTaskResult(String sessionId) {
        TaskSession session = taskSessionMapper.selectById(sessionId);
        if (session == null) {
            return "未找到会话: " + sessionId;
        }
        
        return session.getSummary();
    }
    
    /**
     * 创建会话
     */
    private void createSession(String sessionId, String userInput, String filePath) {
        TaskSession session = new TaskSession();
        session.setSessionId(sessionId);
        session.setUserInput(userInput);
        session.setFilePath(filePath);
        session.setCreateTime(new Date());
        session.setStatus("PENDING");
        
        taskSessionMapper.insert(session);
        log.info("创建会话: {}", sessionId);
    }
    
    /**
     * 更新会话状态
     */
    private void updateSessionStatus(String sessionId, String status, String summary) {
        TaskSession session = taskSessionMapper.selectById(sessionId);
        session.setStatus(status);
        session.setSummary(summary);
        
        taskSessionMapper.updateById(session);
        log.info("更新会话状态: {}, {}", sessionId, status);
    }
    
    /**
     * 保存任务列表到数据库
     */
    private void saveTasks(String sessionId, List<Map<String, Object>> tasks) throws JsonProcessingException {
        int order = 1;
        for (Map<String, Object> task : tasks) {
            TaskInfo taskInfo = new TaskInfo();
            taskInfo.setTaskId(UUID.randomUUID().toString());
            taskInfo.setSessionId(sessionId);
            taskInfo.setTaskOrder(order++);
            taskInfo.setDescription((String) task.get("description"));
            taskInfo.setInputFile((String) task.get("inputFile"));
            
            // 处理输出文件
            List<String> outputFiles = new ArrayList<>();
            outputFiles.add((String) task.get("outputFile")); // 主输出文件
            
            // 如果有其他输出文件（如图表文件），也添加到列表中
            if (task.containsKey("additionalOutputFiles")) {
                outputFiles.addAll((List<String>) task.get("additionalOutputFiles"));
            }
            
            taskInfo.setOutputFiles(objectMapper.writeValueAsString(outputFiles));
            taskInfo.setUsePlugin((Boolean) task.get("usePlugin"));
            
            if (taskInfo.isUsePlugin()) {
                taskInfo.setPluginName((String) task.get("pluginName"));
                taskInfo.setPluginParameters(objectMapper.writeValueAsString(task.get("pluginParameters")));
            }
            
            taskInfo.setStatus("PENDING");
            taskInfo.setCreateTime(new Date());
            
            taskInfoMapper.insert(taskInfo);
            log.info("保存任务: {}, {}", taskInfo.getTaskId(), taskInfo.getDescription());
        }
    }
    
    /**
     * 执行单个任务
     */
    private void executeTask(TaskInfo task) {
        try {
            // 更新任务状态
            task.setStatus("RUNNING");
            task.setStartTime(new Date());
            taskInfoMapper.updateById(task);
            log.info("开始执行任务: {}", task.getTaskId());
            
            // 执行任务
            if (task.isUsePlugin()) {
                // 解析插件参数
                Map<String, Object> parameters = objectMapper.readValue(
                    task.getPluginParameters(), 
                    new TypeReference<Map<String, Object>>() {}
                );
                
                // 根据插件名称执行对应的操作
                if ("Python代码生成插件".equals(task.getPluginName())) {
                    executePythonGenerateTask(task, parameters);
                } else if ("Python代码执行插件".equals(task.getPluginName())) {
                    executePythonExecuteTask(task, parameters);
                } else {
                    throw new RuntimeException("不支持的插件: " + task.getPluginName());
                }
            }
            
            // 更新任务状态
            task.setStatus("COMPLETED");
            task.setEndTime(new Date());
            taskInfoMapper.updateById(task);
            log.info("任务执行完成: {}", task.getTaskId());
            
        } catch (Exception e) {
            log.error("执行任务失败: {}", task.getTaskId(), e);
            
            // 更新任务状态
            task.setStatus("FAILED");
            task.setErrorMessage(e.getMessage());
            task.setEndTime(new Date());
            taskInfoMapper.updateById(task);
        }
    }
    
    /**
     * 执行Python代码生成任务
     */
    private void executePythonGenerateTask(TaskInfo task, Map<String, Object> parameters) throws Exception {
        // 创建请求对象
        PythonGenerateReq req = new PythonGenerateReq();
        req.setFilePath((String) parameters.get("filePath"));
        req.setAnalysisType((String) parameters.get("analysisType"));
        req.setOutputFormat((String) parameters.get("outputFormat"));
        req.setAdditionalRequirements((String) parameters.get("additionalRequirements"));
        
        // 获取插件
        PythonGeneratePlugin plugin = (PythonGeneratePlugin) pluginRegistry.getPlugin("Python代码生成插件");
        
        // 生成Python代码
        String pythonCode = plugin.generatePythonCode(req);
        
        // 保存Python文件
        String pythonFilePath = fileStorageService.storeGeneratedFile(
            pythonCode, 
            "task_" + task.getTaskId() + ".py", 
            "python_scripts"
        );
        
        // 更新任务信息
        task.setPythonFilePath(pythonFilePath);
        taskInfoMapper.updateById(task);
        log.info("生成Python脚本: {}", pythonFilePath);
        
        // 执行Python文件
        executePythonFile(pythonFilePath);
    }
    
    /**
     * 执行Python代码执行任务
     */
    private void executePythonExecuteTask(TaskInfo task, Map<String, Object> parameters) throws Exception {
        String pythonFilePath = (String) parameters.get("pythonFilePath");
        
        // 执行Python文件
        executePythonFile(pythonFilePath);
    }
    
    /**
     * 执行Python文件
     */
    private void executePythonFile(String pythonFilePath) throws Exception {
        // 执行Python文件
        ProcessBuilder processBuilder = new ProcessBuilder("python", pythonFilePath);
        Process process = processBuilder.start();
        
        // 等待执行完成
        int exitCode = process.waitFor();
        
        if (exitCode != 0) {
            throw new RuntimeException("Python脚本执行失败，退出码: " + exitCode);
        }
    }
    
    /**
     * 生成任务执行结果摘要
     */
    private String generateTaskSummary(String sessionId) {
        StringBuilder summary = new StringBuilder();
        
        // 添加会话信息
        TaskSession session = taskSessionMapper.selectById(sessionId);
        summary.append("# 数据处理任务汇总报告\n\n");
        summary.append("## 用户需求\n");
        summary.append(session.getUserInput()).append("\n\n");
        
        // 添加任务执行结果
        List<TaskInfo> tasks = taskInfoMapper.selectBySessionId(sessionId);
        summary.append("## 执行任务\n");
        for (int i = 0; i < tasks.size(); i++) {
            TaskInfo task = tasks.get(i);
            summary.append(String.format("%d. %s\n", i+1, task.getDescription()));
            summary.append("   - 状态: ").append(task.getStatus()).append("\n");
            
            if ("COMPLETED".equals(task.getStatus())) {
                summary.append("   - 输出文件: ").append(task.getOutputFiles()).append("\n");
            } else if ("FAILED".equals(task.getStatus())) {
                summary.append("   - 错误信息: ").append(task.getErrorMessage()).append("\n");
            }
            
            summary.append("\n");
        }
        
        // 添加最终输出
        TaskInfo lastTask = tasks.get(tasks.size() - 1);
        if ("COMPLETED".equals(lastTask.getStatus())) {
            summary.append("## 最终结果\n");
            summary.append("处理完成，最终输出文件: ").append(lastTask.getOutputFiles()).append("\n");
        } else {
            summary.append("## 处理结果\n");
            summary.append("任务未能完全完成，请查看各个任务的状态了解详情。\n");
        }
        
        return summary.toString();
    }
} 