package org.ruoyi.task.domain;

import lombok.Data;
import java.util.Date;

/**
 * 任务会话实体类
 */
@Data
public class TaskSession {
    /**
     * 会话ID
     */
    private String sessionId;
    
    /**
     * 用户输入
     */
    private String userInput;
    
    /**
     * 文件路径
     */
    private String filePath;
    
    /**
     * 创建时间
     */
    private Date createTime;
    
    /**
     * 状态（PENDING, RUNNING, COMPLETED, FAILED）
     */
    private String status;
    
    /**
     * 结果摘要
     */
    private String summary;
} 