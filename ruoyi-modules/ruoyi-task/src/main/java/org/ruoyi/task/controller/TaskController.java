package org.ruoyi.task.controller;

import lombok.RequiredArgsConstructor;
import org.ruoyi.common.core.domain.R;
import org.ruoyi.task.domain.TaskRequest;
import org.ruoyi.task.service.ITaskService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/system/task")
public class TaskController {
    
    private final ITaskService taskService;
    
    /**
     * 执行复杂任务
     */
    @PostMapping("/execute")
    public R<String> executeTask(@RequestBody TaskRequest request) {
        String result = taskService.executeComplexTask(request.getUserInput(), request.getFilePath());
        return R.ok(result);
    }
    
    /**
     * 执行复杂任务（实时推送进度）
     */
    @PostMapping(value = "/executeAsync", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter executeTaskAsync(@RequestBody TaskRequest request) {
        return taskService.executeComplexTaskAsync(request.getUserInput(), request.getFilePath());
    }
    
    /**
     * 获取任务执行结果
     */
    @GetMapping("/result/{sessionId}")
    public R<String> getTaskResult(@PathVariable String sessionId) {
        String result = taskService.getTaskResult(sessionId);
        return R.ok(result);
    }
} 