package org.ruoyi.task.service.impl;

import org.ruoyi.task.domain.PythonExecutionResult;
import org.ruoyi.task.service.PythonExecutionService;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/**
 * Python执行服务实现类
 */
@Service
public class PythonExecutionServiceImpl implements PythonExecutionService {
    
    private static final Logger log = LoggerFactory.getLogger(PythonExecutionServiceImpl.class);
    
    // Python解释器路径
    private static final String PYTHON_PATH = "python";
    
    // 执行超时时间（秒）
    private static final int TIMEOUT_SECONDS = 300;
    
    // 最大内存使用（MB）
    private static final int MAX_MEMORY_MB = 1024;
    
    @Override
    public PythonExecutionResult executePythonCode(String code, String[] inputFiles, String[] outputFiles) {
        try {
            // 创建临时Python文件
            Path tempFile = Files.createTempFile("python_script_", ".py");
            Files.write(tempFile, code.getBytes());
            
            // 执行Python文件
            PythonExecutionResult result = executePythonFile(tempFile.toString(), inputFiles, outputFiles);
            
            // 删除临时文件
            Files.delete(tempFile);
            
            return result;
        } catch (Exception e) {
            log.error("执行Python代码失败", e);
            return PythonExecutionResult.failure("执行Python代码失败: " + e.getMessage());
        }
    }
    
    @Override
    public PythonExecutionResult executePythonFile(String filePath, String[] inputFiles, String[] outputFiles) {
        long startTime = System.currentTimeMillis();
        
        try {
            // 检查Python环境
            if (!checkPythonEnvironment()) {
                return PythonExecutionResult.failure("Python环境检查失败");
            }
            
            // 构建命令
            List<String> command = new ArrayList<>();
            command.add(PYTHON_PATH);
            command.add(filePath);
            
            // 添加输入文件参数
            if (inputFiles != null) {
                for (String file : inputFiles) {
                    command.add(file);
                }
            }
            
            // 添加输出文件参数
            if (outputFiles != null) {
                for (String file : outputFiles) {
                    command.add(file);
                }
            }
            
            // 创建进程构建器
            ProcessBuilder processBuilder = new ProcessBuilder(command);
            processBuilder.redirectErrorStream(true);
            
            // 设置环境变量
            processBuilder.environment().put("PYTHONPATH", System.getenv("PYTHONPATH"));
            
            // 启动进程
            Process process = processBuilder.start();
            
            // 创建输出读取器
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            
            // 创建执行器服务
            ExecutorService executor = Executors.newSingleThreadExecutor();
            
            // 读取输出
            Future<String> outputFuture = executor.submit(() -> {
                StringBuilder output = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                }
                return output.toString();
            });
            
            // 等待进程完成或超时
            boolean completed = process.waitFor(TIMEOUT_SECONDS, TimeUnit.SECONDS);
            
            // 关闭执行器
            executor.shutdownNow();
            
            // 获取执行时间
            long executionTime = System.currentTimeMillis() - startTime;
            
            // 获取内存使用
            double memoryUsage = getMemoryUsage(process);
            
            if (!completed) {
                process.destroyForcibly();
                return PythonExecutionResult.failure("执行超时");
            }
            
            // 获取输出
            String output = outputFuture.get();
            
            // 检查执行结果
            if (process.exitValue() == 0) {
                return PythonExecutionResult.success(output, outputFiles, executionTime, memoryUsage);
            } else {
                return PythonExecutionResult.failure("执行失败，退出码: " + process.exitValue() + "\n" + output);
            }
            
        } catch (Exception e) {
            log.error("执行Python文件失败", e);
            return PythonExecutionResult.failure("执行Python文件失败: " + e.getMessage());
        }
    }
    
    @Override
    public boolean checkPythonEnvironment() {
        try {
            Process process = Runtime.getRuntime().exec(PYTHON_PATH + " --version");
            return process.waitFor() == 0;
        } catch (Exception e) {
            log.error("检查Python环境失败", e);
            return false;
        }
    }
    
    @Override
    public String getPythonVersion() {
        try {
            Process process = Runtime.getRuntime().exec(PYTHON_PATH + " --version");
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            return reader.readLine();
        } catch (Exception e) {
            log.error("获取Python版本失败", e);
            return "未知";
        }
    }
    
    /**
     * 获取进程内存使用
     */
    private double getMemoryUsage(Process process) {
        try {
            // 这里使用简单的估算方法，实际项目中可能需要更精确的内存监控
            return process.getInputStream().available() / (1024.0 * 1024.0);
        } catch (Exception e) {
            return 0.0;
        }
    }
} 