package org.ruoyi.task.service;

import org.ruoyi.task.domain.PythonExecutionResult;

/**
 * Python执行服务接口
 */
public interface PythonExecutionService {
    /**
     * 执行Python代码
     *
     * @param code Python代码
     * @param inputFiles 输入文件列表
     * @param outputFiles 输出文件列表
     * @return 执行结果
     */
    PythonExecutionResult executePythonCode(String code, String[] inputFiles, String[] outputFiles);

    /**
     * 执行Python文件
     *
     * @param filePath Python文件路径
     * @param inputFiles 输入文件列表
     * @param outputFiles 输出文件列表
     * @return 执行结果
     */
    PythonExecutionResult executePythonFile(String filePath, String[] inputFiles, String[] outputFiles);

    /**
     * 检查Python环境
     *
     * @return 环境检查结果
     */
    boolean checkPythonEnvironment();

    /**
     * 获取Python版本
     *
     * @return Python版本信息
     */
    String getPythonVersion();
} 