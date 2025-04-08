package org.ruoyi.task.service;

import org.ruoyi.task.domain.TaskExecutionResult;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * 任务服务接口
 */
public interface ITaskService {
    
    /**
     * 执行复杂任务
     * 
     * @param userInput 用户输入
     * @param filePath 文件路径
     * @return 执行结果
     */
    String executeComplexTask(String userInput, String filePath);
    
    /**
     * 异步执行复杂任务，支持实时进度推送
     * 
     * @param userInput 用户输入
     * @param filePath 文件路径
     * @return SseEmitter用于推送执行进度
     */
    SseEmitter executeComplexTaskAsync(String userInput, String filePath);
    
    /**
     * 获取任务执行结果
     * 
     * @param sessionId 会话ID
     * @return 执行结果摘要
     */
    String getTaskResult(String sessionId);
} 