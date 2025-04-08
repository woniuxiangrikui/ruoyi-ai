package org.ruoyi.task.domain;

import lombok.Data;

/**
 * 任务请求实体类
 */
@Data
public class TaskRequest {
    /**
     * 用户输入（需求描述）
     */
    private String userInput;
    
    /**
     * Excel文件路径
     */
    private String filePath;
} 