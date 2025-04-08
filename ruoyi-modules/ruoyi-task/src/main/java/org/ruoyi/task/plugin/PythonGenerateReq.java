package org.ruoyi.task.plugin;

import lombok.Data;

/**
 * Python生成插件请求参数
 */
@Data
public class PythonGenerateReq {
    /**
     * 用户的需求描述
     */
    private String requirement;
    
    /**
     * 上传的Excel文件路径
     */
    private String excelFilePath;
    
    /**
     * 生成的Python文件保存路径
     */
    private String outputPythonPath;
} 