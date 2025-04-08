package org.ruoyi.task.domain;

import lombok.Data;

/**
 * 重试策略实体类
 */
@Data
public class RetryPolicy {
    /**
     * 最大重试次数
     */
    private int maxRetries = 3;
    
    /**
     * 重试间隔（毫秒）
     */
    private long retryInterval = 1000;
    
    /**
     * 是否使用指数退避
     */
    private boolean useExponentialBackoff = true;
    
    /**
     * 最大重试间隔（毫秒）
     */
    private long maxRetryInterval = 60000;
    
    /**
     * 重试条件（可选）
     */
    private String retryCondition;
} 