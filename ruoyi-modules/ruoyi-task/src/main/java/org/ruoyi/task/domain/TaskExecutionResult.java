package org.ruoyi.task.domain;

import lombok.Data;
import java.util.List;

@Data
public class TaskExecutionResult {
    private String userInput;
    private String analysis;
    private List<Task> tasks;
    private String result;
} 