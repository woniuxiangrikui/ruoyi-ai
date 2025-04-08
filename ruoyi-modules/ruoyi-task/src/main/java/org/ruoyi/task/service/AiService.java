package org.ruoyi.task.service;

/**
 * AI服务接口
 */
public interface AiService {
    
    /**
     * 生成任务计划
     * 
     * @param prompt 任务计划提示
     * @return 任务计划JSON
     */
    String generateTaskPlan(String prompt);
} 