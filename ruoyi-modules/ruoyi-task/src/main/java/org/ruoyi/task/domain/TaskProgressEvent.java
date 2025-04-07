package org.ruoyi.task.domain;

import lombok.Data;
import java.util.List;

@Data
public class TaskProgressEvent {
    public enum EventType {
        PLAN_GENERATED,     // 任务计划生成
        TASK_STARTED,       // 任务开始执行
        TASK_COMPLETED,     // 任务完成
        TASK_FAILED,        // 任务失败
        ALL_COMPLETED       // 所有任务完成
    }
    
    private EventType type;
    private String analysis;
    private List<Task> tasks;
    private Integer currentTaskId;
    private String result;
    private String timestamp;
} 