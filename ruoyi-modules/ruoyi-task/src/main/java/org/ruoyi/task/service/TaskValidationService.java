package org.ruoyi.task.service;

import org.ruoyi.task.domain.TaskRequest;

/**
 * 任务验证服务接口
 */
public interface TaskValidationService {
    /**
     * 验证任务输入
     *
     * @param request 任务请求
     * @return 验证结果
     */
    boolean validateInput(TaskRequest request);

    /**
     * 验证文件
     *
     * @param filePath 文件路径
     * @return 验证结果
     */
    boolean validateFile(String filePath);

    /**
     * 验证权限
     *
     * @param userId 用户ID
     * @param taskId 任务ID
     * @return 验证结果
     */
    boolean validatePermissions(String userId, String taskId);

    /**
     * 验证任务参数
     *
     * @param taskId 任务ID
     * @param parameters 任务参数
     * @return 验证结果
     */
    boolean validateParameters(String taskId, String parameters);

    /**
     * 验证任务依赖
     *
     * @param taskId 任务ID
     * @return 验证结果
     */
    boolean validateDependencies(String taskId);

    /**
     * 获取验证错误信息
     *
     * @return 错误信息
     */
    String getValidationError();
} 