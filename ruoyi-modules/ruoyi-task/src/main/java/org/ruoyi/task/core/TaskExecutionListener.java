package org.ruoyi.task.core;

import org.ruoyi.task.domain.Task;
import org.ruoyi.task.domain.TaskPlan;

public interface TaskExecutionListener {
    void onPlanGenerated(TaskPlan plan);
    void onTaskStarted(Task task);
    void onTaskCompleted(Task task, String result);
    void onTaskFailed(Task task, String errorMessage);
    void onAllTasksCompleted(String finalResult);
} 