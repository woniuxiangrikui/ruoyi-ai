package org.ruoyi.task.service;

import org.ruoyi.task.domain.TaskStatistics;

/**
 * 任务监控服务接口
 */
public interface TaskMonitorService {
    /**
     * 记录任务开始
     *
     * @param taskId 任务ID
     */
    void recordTaskStart(String taskId);

    /**
     * 记录任务结束
     *
     * @param taskId 任务ID
     * @param success 是否成功
     */
    void recordTaskEnd(String taskId, boolean success);

    /**
     * 记录任务错误
     *
     * @param taskId 任务ID
     * @param e 异常信息
     */
    void recordTaskError(String taskId, Exception e);

    /**
     * 获取任务统计信息
     *
     * @param taskId 任务ID
     * @return 任务统计信息
     */
    TaskStatistics getTaskStatistics(String taskId);
} 