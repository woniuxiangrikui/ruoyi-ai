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
import org.springframework.stereotype.Component;
import cn.hutool.json.JSONUtil;

import java.util.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class TaskExecutor {
    private final OpenAiClient openAiClient;
    private final PluginRegistry pluginRegistry;
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    /**
     * 执行任务计划
     */
    public String executeTasks(TaskPlan taskPlan, String userInput) {
        return executeTasks(taskPlan, userInput, null);
    }
    
    /**
     * 执行任务计划（带监听器）
     */
    public String executeTasks(TaskPlan taskPlan, String userInput, TaskExecutionListener listener) {
        // 初始化上下文，存储任务执行结果
        Map<Integer, String> taskResults = new HashMap<>();
        List<Task> sortedTasks = sortTasksByDependency(taskPlan.getTasks());
        StringBuilder finalResult = new StringBuilder();
        
        // 添加分析结果
        finalResult.append("分析：").append(taskPlan.getAnalysis()).append("\n\n");
        
        // 通知任务计划已生成
        if (listener != null) {
            listener.onPlanGenerated(taskPlan);
        }
        
        // 按顺序执行任务
        for (Task task : sortedTasks) {
            try {
                log.info("开始执行任务: {}", task.getTaskId());
                task.setStatus(Task.TaskStatus.RUNNING);
                
                // 通知任务开始
                if (listener != null) {
                    listener.onTaskStarted(task);
                }
                
                // 准备任务输入（合并用户输入和依赖任务的结果）
                String taskInput = prepareTaskInput(task, userInput, taskResults);
                
                // 执行单个任务
                String result = executeTask(task.getFunctionName(), taskInput);
                
                // 存储结果
                task.setResult(result);
                task.setStatus(Task.TaskStatus.COMPLETED);
                taskResults.put(task.getTaskId(), result);
                
                // 添加到最终结果
                finalResult.append("任务").append(task.getTaskId()).append(": ")
                    .append(task.getDescription()).append("\n")
                    .append("结果: ").append(result).append("\n\n");
                
                // 通知任务完成
                if (listener != null) {
                    listener.onTaskCompleted(task, result);
                }
                
                log.info("任务{}执行完成", task.getTaskId());
            } catch (Exception e) {
                log.error("任务{}执行失败", task.getTaskId(), e);
                task.setStatus(Task.TaskStatus.FAILED);
                task.setResult("执行失败: " + e.getMessage());
                finalResult.append("任务").append(task.getTaskId()).append("执行失败: ")
                    .append(e.getMessage()).append("\n\n");
                
                // 通知任务失败
                if (listener != null) {
                    listener.onTaskFailed(task, e.getMessage());
                }
            }
        }
        
        // 最终整合
        String summaryResult = generateSummary(taskPlan, taskResults, userInput);
        finalResult.append("最终结果：\n").append(summaryResult);
        
        // 通知所有任务完成
        if (listener != null) {
            listener.onAllTasksCompleted(summaryResult);
        }
        
        return finalResult.toString();
    }
    
    /**
     * 准备任务输入
     */
    private String prepareTaskInput(Task task, String userInput, Map<Integer, String> taskResults) {
        StringBuilder input = new StringBuilder(userInput);
        
        // 添加前置任务的结果
        if (task.getDependsOn() != null && !task.getDependsOn().isEmpty()) {
            input.append("\n\n前置任务结果:\n");
            for (Integer dependTaskId : task.getDependsOn()) {
                String dependResult = taskResults.get(dependTaskId);
                if (dependResult != null) {
                    input.append("任务").append(dependTaskId).append("结果: ")
                        .append(dependResult).append("\n");
                }
            }
        }
        
        return input.toString();
    }
    
    /**
     * 执行单个任务
     */
    @SuppressWarnings("unchecked")
    private <R extends PluginParam, T> String executeTask(String functionName, String taskInput) {
        // 获取插件
        PluginAbstract<R, T> plugin = (PluginAbstract<R, T>) pluginRegistry.getPlugin(functionName);
        if (plugin == null) {
            throw new RuntimeException("未找到插件: " + functionName);
        }
        
        // 构建函数定义
        Functions functions = Functions.builder()
                .name(plugin.getFunction())
                .description(plugin.getDescription())
                .parameters(plugin.getParameters())
                .build();
                
        // 构建消息
        List<Message> messages = new ArrayList<>();
        messages.add(Message.builder().role(Message.Role.USER).content(taskInput).build());
        
        // 构建请求
        ChatCompletion chatCompletion = ChatCompletion.builder()
                .messages(messages)
                .model("gpt-4-turbo")
                .functionCall("auto")
                .functions(Collections.singletonList(functions))
                .build();
                
        // 调用模型获取函数调用
        ChatCompletionResponse functionCallResponse = openAiClient.chatCompletion(chatCompletion);
        ChatChoice chatChoice = functionCallResponse.getChoices().get(0);
        
        if (chatChoice.getMessage().getFunctionCall() == null) {
            // 模型选择不调用函数
            return chatChoice.getMessage().getContent();
        }
        
        // 提取参数并执行函数
        String arguments = chatChoice.getMessage().getFunctionCall().getArguments();
        R functionParam = (R) JSONUtil.toBean(arguments, plugin.getR());
        T result = plugin.func(functionParam);
        
        // 构建函数调用记录
        FunctionCall functionCall = FunctionCall.builder()
                .arguments(arguments)
                .name(plugin.getFunction())
                .build();
                
        // 添加函数调用和结果到消息历史
        messages.add(Message.builder()
                .role(Message.Role.ASSISTANT)
                .content("function_call")
                .functionCall(functionCall)
                .build());
                
        messages.add(Message.builder()
                .role(Message.Role.FUNCTION)
                .name(plugin.getFunction())
                .content(plugin.content(result))
                .build());
                
        // 让模型生成基于函数结果的回复
        chatCompletion.setFunctionCall(null);
        chatCompletion.setFunctions(null);
        
        ChatCompletionResponse response = openAiClient.chatCompletion(chatCompletion);
        return response.getChoices().get(0).getMessage().getContent();
    }
    
    /**
     * 基于所有任务结果生成最终摘要
     */
    private String generateSummary(TaskPlan taskPlan, Map<Integer, String> taskResults, String userInput) {
        try {
            // 构建系统提示词
            String systemPrompt = "你是一个结果整合助手。用户的原始需求是：\n\n" + userInput 
                + "\n\n基于这个需求，系统执行了以下任务，请整合所有任务的结果，生成一个完整、清晰的最终回答：";
            
            // 构建任务结果描述
            StringBuilder taskResultsStr = new StringBuilder();
            for (Task task : taskPlan.getTasks()) {
                String result = taskResults.get(task.getTaskId());
                if (result != null) {
                    taskResultsStr.append("任务").append(task.getTaskId()).append(": ")
                        .append(task.getDescription()).append("\n")
                        .append("结果: ").append(result).append("\n\n");
                }
            }
            
            // 构建消息
            List<Message> messages = new ArrayList<>();
            messages.add(Message.builder().role(Message.Role.SYSTEM).content(systemPrompt).build());
            messages.add(Message.builder().role(Message.Role.USER).content(taskResultsStr.toString()).build());
            
            // 调用模型
            ChatCompletion chatCompletion = ChatCompletion.builder()
                .messages(messages)
                .model("gpt-4-turbo")
                .build();
                
            ChatCompletionResponse response = openAiClient.chatCompletion(chatCompletion);
            return response.getChoices().get(0).getMessage().getContent();
            
        } catch (Exception e) {
            log.error("生成最终摘要失败", e);
            return "无法生成最终摘要: " + e.getMessage();
        }
    }
    
    /**
     * 按依赖关系排序任务
     */
    private List<Task> sortTasksByDependency(List<Task> tasks) {
        Map<Integer, Task> taskMap = new HashMap<>();
        for (Task task : tasks) {
            taskMap.put(task.getTaskId(), task);
        }
            
        List<Task> result = new ArrayList<>();
        Set<Integer> visited = new HashSet<>();
        
        for (Task task : tasks) {
            if (!visited.contains(task.getTaskId())) {
                dfs(task, taskMap, visited, result);
            }
        }
        
        return result;
    }
    
    private void dfs(Task task, Map<Integer, Task> taskMap, Set<Integer> visited, List<Task> result) {
        visited.add(task.getTaskId());
        
        if (task.getDependsOn() != null) {
            for (Integer dependId : task.getDependsOn()) {
                if (!visited.contains(dependId) && taskMap.containsKey(dependId)) {
                    dfs(taskMap.get(dependId), taskMap, visited, result);
                }
            }
        }
        
        result.add(task);
    }
} 