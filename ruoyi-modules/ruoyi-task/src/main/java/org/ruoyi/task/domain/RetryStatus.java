package org.ruoyi.task.domain;

import lombok.Data;
import java.util.Date;

/**
 * 重试状态实体类
 */
@Data
public class RetryStatus {
    /**
     * 任务ID
     */
    private String taskId;
    
    /**
     * 当前重试次数
     */
    private int currentRetries;
    
    /**
     * 上次重试时间
     */
    private Date lastRetryTime;
    
    /**
     * 下次重试时间
     */
    private Date nextRetryTime;
    
    /**
     * 重试状态（PENDING, RETRYING, SUCCEEDED, FAILED）
     */
    private String status;
    
    /**
     * 最后一次错误信息
     */
    private String lastError;
} 