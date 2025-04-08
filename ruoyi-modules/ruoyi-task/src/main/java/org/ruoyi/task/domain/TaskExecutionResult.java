package org.ruoyi.task.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 任务执行结果
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TaskExecutionResult {
    
    /**
     * 执行是否成功
     */
    private boolean success;
    
    /**
     * 结果摘要或错误信息
     */
    private String message;
    
    /**
     * 所有任务的执行结果
     */
    private List<Object> results;
} 