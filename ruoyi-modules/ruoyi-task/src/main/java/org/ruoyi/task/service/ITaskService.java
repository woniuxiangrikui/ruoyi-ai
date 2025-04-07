package org.ruoyi.task.service;

import org.ruoyi.task.domain.TaskExecutionResult;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

public interface ITaskService {
    /**
     * 执行复杂任务
     * @param userInput 用户输入
     * @return 任务执行结果
     */
    TaskExecutionResult executeComplexTask(String userInput);
    
    /**
     * 异步执行复杂任务，支持实时进度推送
     * @param userInput 用户输入
     * @return SSE发射器
     */
    SseEmitter executeComplexTaskAsync(String userInput);
} 