package org.ruoyi.task.domain;

import lombok.Data;
import java.util.Date;

/**
 * 任务统计信息实体类
 */
@Data
public class TaskStatistics {
    /**
     * 任务ID
     */
    private String taskId;
    
    /**
     * 开始时间
     */
    private Date startTime;
    
    /**
     * 结束时间
     */
    private Date endTime;
    
    /**
     * 执行时长（毫秒）
     */
    private long duration;
    
    /**
     * 是否成功
     */
    private boolean success;
    
    /**
     * 错误信息
     */
    private String errorMessage;
    
    /**
     * 内存使用（MB）
     */
    private long memoryUsage;
    
    /**
     * CPU使用率（%）
     */
    private double cpuUsage;
    
    /**
     * 重试次数
     */
    private int retryCount;
} 