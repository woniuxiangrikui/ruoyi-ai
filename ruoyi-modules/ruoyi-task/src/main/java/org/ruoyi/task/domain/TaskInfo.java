package org.ruoyi.task.domain;

import lombok.Data;
import java.util.Date;

/**
 * 任务信息实体类
 */
@Data
public class TaskInfo {
    /**
     * 任务ID
     */
    private String taskId;
    
    /**
     * 会话ID
     */
    private String sessionId;
    
    /**
     * 任务顺序
     */
    private int taskOrder;
    
    /**
     * 任务描述
     */
    private String description;
    
    /**
     * 输入文件
     */
    private String inputFile;
    
    /**
     * 输出文件列表（JSON格式）
     */
    private String outputFiles;
    
    /**
     * 是否使用插件
     */
    private boolean usePlugin;
    
    /**
     * 插件名称
     */
    private String pluginName;
    
    /**
     * 插件参数（JSON格式）
     */
    private String pluginParameters;
    
    /**
     * Python文件路径
     */
    private String pythonFilePath;
    
    /**
     * 任务状态（PENDING, RUNNING, COMPLETED, FAILED）
     */
    private String status;
    
    /**
     * 错误信息
     */
    private String errorMessage;
    
    /**
     * 创建时间
     */
    private Date createTime;
    
    /**
     * 开始时间
     */
    private Date startTime;
    
    /**
     * 结束时间
     */
    private Date endTime;
} 