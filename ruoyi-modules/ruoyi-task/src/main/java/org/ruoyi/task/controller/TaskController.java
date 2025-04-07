package org.ruoyi.task.controller;

import lombok.RequiredArgsConstructor;
import org.ruoyi.common.core.domain.R;
import org.ruoyi.task.domain.TaskExecutionResult;
import org.ruoyi.task.service.ITaskService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequiredArgsConstructor
@RequestMapping("/system/task")
public class TaskController {
    
    private final ITaskService taskService;
    
    /**
     * 执行复杂任务
     */
    @PostMapping("/execute")
    public R<TaskExecutionResult> executeTask(@RequestBody String userInput) {
        TaskExecutionResult result = taskService.executeComplexTask(userInput);
        return R.ok(result);
    }
    
    /**
     * 执行复杂任务（实时推送进度）
     */
    @GetMapping(value = "/executeAsync", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter executeTaskAsync(@RequestParam String userInput) {
        return taskService.executeComplexTaskAsync(userInput);
    }
} 