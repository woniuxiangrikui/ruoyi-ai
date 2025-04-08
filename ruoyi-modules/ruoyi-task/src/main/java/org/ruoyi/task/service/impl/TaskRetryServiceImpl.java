package org.ruoyi.task.service.impl;

import org.ruoyi.task.domain.RetryPolicy;
import org.ruoyi.task.domain.RetryStatus;
import org.ruoyi.task.service.TaskRetryService;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 任务重试服务实现类
 */
@Service
public class TaskRetryServiceImpl implements TaskRetryService {
    
    private static final Logger log = LoggerFactory.getLogger(TaskRetryServiceImpl.class);
    
    // 存储任务的重试策略
    private final Map<String, RetryPolicy> retryPolicies = new ConcurrentHashMap<>();
    
    // 存储任务的重试状态
    private final Map<String, RetryStatus> retryStatuses = new ConcurrentHashMap<>();
    
    @Override
    public void retryTask(String taskId) {
        RetryStatus status = retryStatuses.get(taskId);
        RetryPolicy policy = retryPolicies.get(taskId);
        
        if (status == null) {
            status = new RetryStatus();
            status.setTaskId(taskId);
            status.setCurrentRetries(0);
            status.setStatus("PENDING");
            retryStatuses.put(taskId, status);
        }
        
        if (policy == null) {
            policy = new RetryPolicy(); // 使用默认策略
            retryPolicies.put(taskId, policy);
        }
        
        // 检查是否超过最大重试次数
        if (status.getCurrentRetries() >= policy.getMaxRetries()) {
            status.setStatus("FAILED");
            status.setLastError("超过最大重试次数: " + policy.getMaxRetries());
            log.warn("任务 {} 重试失败，已达到最大重试次数", taskId);
            return;
        }
        
        // 计算下次重试时间
        long nextInterval = calculateNextRetryInterval(status.getCurrentRetries(), policy);
        Date nextRetryTime = new Date(System.currentTimeMillis() + nextInterval);
        
        // 更新重试状态
        status.setCurrentRetries(status.getCurrentRetries() + 1);
        status.setLastRetryTime(new Date());
        status.setNextRetryTime(nextRetryTime);
        status.setStatus("RETRYING");
        
        log.info("任务 {} 将在 {} 后重试，当前重试次数: {}", 
                taskId, nextInterval, status.getCurrentRetries());
    }
    
    @Override
    public void setRetryPolicy(String taskId, RetryPolicy policy) {
        if (policy == null) {
            retryPolicies.remove(taskId);
        } else {
            retryPolicies.put(taskId, policy);
        }
        log.info("为任务 {} 设置重试策略: 最大重试次数={}, 重试间隔={}ms", 
                taskId, policy.getMaxRetries(), policy.getRetryInterval());
    }
    
    @Override
    public RetryStatus getRetryStatus(String taskId) {
        return retryStatuses.get(taskId);
    }
    
    /**
     * 计算下次重试间隔
     *
     * @param currentRetries 当前重试次数
     * @param policy 重试策略
     * @return 下次重试间隔（毫秒）
     */
    private long calculateNextRetryInterval(int currentRetries, RetryPolicy policy) {
        if (!policy.isUseExponentialBackoff()) {
            return policy.getRetryInterval();
        }
        
        // 使用指数退避算法: interval * 2^retries
        long interval = policy.getRetryInterval() * (long) Math.pow(2, currentRetries);
        
        // 确保不超过最大重试间隔
        return Math.min(interval, policy.getMaxRetryInterval());
    }
    
    /**
     * 更新任务重试状态
     *
     * @param taskId 任务ID
     * @param success 是否成功
     * @param error 错误信息
     */
    public void updateRetryStatus(String taskId, boolean success, String error) {
        RetryStatus status = retryStatuses.get(taskId);
        if (status != null) {
            if (success) {
                status.setStatus("SUCCEEDED");
                status.setLastError(null);
                log.info("任务 {} 重试成功", taskId);
            } else {
                status.setStatus("FAILED");
                status.setLastError(error);
                log.error("任务 {} 重试失败: {}", taskId, error);
            }
        }
    }
} 