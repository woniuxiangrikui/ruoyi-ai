package org.ruoyi.task.domain;

import lombok.Data;
import java.util.List;

@Data
public class Task {
    private int taskId;
    private String description;
    private String functionName;
    private List<Integer> dependsOn;
    private TaskStatus status = TaskStatus.PENDING;
    private String result;
    
    public enum TaskStatus {
        PENDING, RUNNING, COMPLETED, FAILED
    }
} 