package org.ruoyi.task.domain;

import lombok.Data;
import java.util.List;

@Data
public class TaskPlan {
    private String analysis;
    private List<Task> tasks;
} 