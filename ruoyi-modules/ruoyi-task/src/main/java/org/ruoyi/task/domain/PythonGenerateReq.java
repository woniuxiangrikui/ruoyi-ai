package org.ruoyi.task.domain;

import lombok.Data;

/**
 * Python代码生成请求参数
 */
@Data
public class PythonGenerateReq {
    /**
     * 要处理的文件路径
     */
    private String filePath;
    
    /**
     * 分析类型，如清洗、转换、统计等
     */
    private String analysisType;
    
    /**
     * 输出结果的格式，如CSV、Excel、图表等
     */
    private String outputFormat;
    
    /**
     * 其他特殊需求或说明
     */
    private String additionalRequirements;
} 