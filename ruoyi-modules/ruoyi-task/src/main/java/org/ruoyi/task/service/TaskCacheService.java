package org.ruoyi.task.service;

/**
 * 任务缓存服务接口
 */
public interface TaskCacheService {
    /**
     * 缓存任务结果
     *
     * @param taskId 任务ID
     * @param result 任务结果
     */
    void cacheTaskResult(String taskId, Object result);

    /**
     * 获取缓存的任务结果
     *
     * @param taskId 任务ID
     * @return 任务结果
     */
    Object getCachedResult(String taskId);

    /**
     * 使缓存失效
     *
     * @param taskId 任务ID
     */
    void invalidateCache(String taskId);

    /**
     * 缓存插件
     *
     * @param pluginName 插件名称
     * @param plugin 插件实例
     */
    void cachePlugin(String pluginName, Object plugin);

    /**
     * 获取缓存的插件
     *
     * @param pluginName 插件名称
     * @return 插件实例
     */
    Object getCachedPlugin(String pluginName);

    /**
     * 缓存会话状态
     *
     * @param sessionId 会话ID
     * @param state 会话状态
     */
    void cacheSessionState(String sessionId, Object state);

    /**
     * 获取缓存的会话状态
     *
     * @param sessionId 会话ID
     * @return 会话状态
     */
    Object getCachedSessionState(String sessionId);

    /**
     * 清除所有缓存
     */
    void clearAllCache();
} 