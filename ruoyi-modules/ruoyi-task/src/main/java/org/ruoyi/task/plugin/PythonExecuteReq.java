package org.ruoyi.task.plugin;

import lombok.Data;

/**
 * Python执行插件请求参数
 */
@Data
public class PythonExecuteReq {
    /**
     * 要执行的Python文件路径
     */
    private String pythonFilePath;
    
    /**
     * 输入Excel文件路径（如果有）
     */
    private String inputExcelPath;
    
    /**
     * 输出Excel文件保存路径
     */
    private String outputExcelPath;
} 