package org.ruoyi.task.listener;

import lombok.extern.slf4j.Slf4j;
import org.ruoyi.task.core.TaskExecutionListener;
import org.ruoyi.task.domain.Task;
import org.ruoyi.task.domain.TaskPlan;
import org.ruoyi.task.domain.TaskProgressEvent;
import org.ruoyi.task.utils.SseEmitterUtils;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Date;

@Slf4j
public class SseTaskExecutionListener implements TaskExecutionListener {
    private final SseEmitter emitter;
    
    public SseTaskExecutionListener(SseEmitter emitter) {
        this.emitter = emitter;
    }
    
    @Override
    public void onPlanGenerated(TaskPlan plan) {
        TaskProgressEvent event = new TaskProgressEvent();
        event.setType(TaskProgressEvent.EventType.PLAN_GENERATED);
        event.setAnalysis(plan.getAnalysis());
        event.setTasks(plan.getTasks());
        event.setTimestamp(new Date());
        SseEmitterUtils.sendTaskProgressEvent(emitter, event);
    }
    
    @Override
    public void onTaskStarted(Task task) {
        TaskProgressEvent event = new TaskProgressEvent();
        event.setType(TaskProgressEvent.EventType.TASK_STARTED);
        event.setCurrentTaskId(task.getTaskId());
        event.setTimestamp(new Date());
        SseEmitterUtils.sendTaskProgressEvent(emitter, event);
    }
    
    @Override
    public void onTaskCompleted(Task task, String result) {
        TaskProgressEvent event = new TaskProgressEvent();
        event.setType(TaskProgressEvent.EventType.TASK_COMPLETED);
        event.setCurrentTaskId(task.getTaskId());
        event.setResult(result);
        event.setTimestamp(new Date());
        SseEmitterUtils.sendTaskProgressEvent(emitter, event);
    }
    
    @Override
    public void onTaskFailed(Task task, String errorMessage) {
        TaskProgressEvent event = new TaskProgressEvent();
        event.setType(TaskProgressEvent.EventType.TASK_FAILED);
        event.setCurrentTaskId(task.getTaskId());
        event.setResult("错误: " + errorMessage);
        event.setTimestamp(new Date());
        SseEmitterUtils.sendTaskProgressEvent(emitter, event);
    }
    
    @Override
    public void onAllTasksCompleted(String finalResult) {
        TaskProgressEvent event = new TaskProgressEvent();
        event.setType(TaskProgressEvent.EventType.ALL_COMPLETED);
        event.setResult(finalResult);
        event.setTimestamp(new Date());
        SseEmitterUtils.sendTaskProgressEvent(emitter, event);
        SseEmitterUtils.complete(emitter);
    }
} 