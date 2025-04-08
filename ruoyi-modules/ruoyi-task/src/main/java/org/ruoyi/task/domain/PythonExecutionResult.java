package org.ruoyi.task.domain;

import lombok.Data;

/**
 * Python执行结果实体类
 */
@Data
public class PythonExecutionResult {
    /**
     * 执行是否成功
     */
    private boolean success;

    /**
     * 执行输出
     */
    private String output;

    /**
     * 错误信息
     */
    private String error;

    /**
     * 执行时间（毫秒）
     */
    private long executionTime;

    /**
     * 内存使用（MB）
     */
    private double memoryUsage;

    /**
     * 输出文件列表
     */
    private String[] outputFiles;

    /**
     * 创建成功结果
     */
    public static PythonExecutionResult success(String output, String[] outputFiles, long executionTime, double memoryUsage) {
        PythonExecutionResult result = new PythonExecutionResult();
        result.setSuccess(true);
        result.setOutput(output);
        result.setOutputFiles(outputFiles);
        result.setExecutionTime(executionTime);
        result.setMemoryUsage(memoryUsage);
        return result;
    }

    /**
     * 创建失败结果
     */
    public static PythonExecutionResult failure(String error) {
        PythonExecutionResult result = new PythonExecutionResult();
        result.setSuccess(false);
        result.setError(error);
        return result;
    }
} 