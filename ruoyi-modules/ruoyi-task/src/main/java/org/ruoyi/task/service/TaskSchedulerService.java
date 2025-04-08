package org.ruoyi.task.service;

import org.ruoyi.task.domain.TaskInfo;
import java.util.List;

/**
 * 任务调度服务接口
 */
public interface TaskSchedulerService {
    /**
     * 调度任务
     *
     * @param task 任务信息
     */
    void scheduleTask(TaskInfo task);

    /**
     * 取消任务
     *
     * @param taskId 任务ID
     */
    void cancelTask(String taskId);

    /**
     * 更新任务优先级
     *
     * @param taskId 任务ID
     * @param priority 优先级
     */
    void updateTaskPriority(String taskId, int priority);

    /**
     * 获取已调度的任务列表
     *
     * @return 任务列表
     */
    List<TaskInfo> getScheduledTasks();

    /**
     * 暂停任务
     *
     * @param taskId 任务ID
     */
    void pauseTask(String taskId);

    /**
     * 恢复任务
     *
     * @param taskId 任务ID
     */
    void resumeTask(String taskId);

    /**
     * 获取任务依赖关系
     *
     * @param taskId 任务ID
     * @return 依赖任务ID列表
     */
    List<String> getTaskDependencies(String taskId);

    /**
     * 设置任务依赖关系
     *
     * @param taskId 任务ID
     * @param dependencies 依赖任务ID列表
     */
    void setTaskDependencies(String taskId, List<String> dependencies);
} 