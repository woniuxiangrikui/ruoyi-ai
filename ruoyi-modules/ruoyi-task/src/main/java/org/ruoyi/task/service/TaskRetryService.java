package org.ruoyi.task.service;

import org.ruoyi.task.domain.RetryPolicy;
import org.ruoyi.task.domain.RetryStatus;

/**
 * 任务重试服务接口
 */
public interface TaskRetryService {
    /**
     * 重试任务
     *
     * @param taskId 任务ID
     */
    void retryTask(String taskId);

    /**
     * 设置重试策略
     *
     * @param taskId 任务ID
     * @param policy 重试策略
     */
    void setRetryPolicy(String taskId, RetryPolicy policy);

    /**
     * 获取重试状态
     *
     * @param taskId 任务ID
     * @return 重试状态
     */
    RetryStatus getRetryStatus(String taskId);
} 