package org.ruoyi.task.plugin;

import lombok.extern.slf4j.Slf4j;
import org.ruoyi.common.chat.openai.plugin.PluginAbstract;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Python代码执行插件
 * 执行Python脚本并生成新的Excel文件
 */
@Slf4j
@Component
public class PythonExecutePlugin extends PluginAbstract<PythonExecuteReq> {

    // 默认Python解释器路径
    private static final String DEFAULT_PYTHON_INTERPRETER = "python";
    
    // 脚本执行超时时间(秒)
    private static final int EXECUTION_TIMEOUT = 300;

    public PythonExecutePlugin() {
        super(PythonExecuteReq.class);
        this.setName("Python代码执行插件");
        this.setFunction("executePythonCode");
        this.setDescription("执行Python脚本并生成新的Excel文件，可用于数据处理和分析任务");

        List<Arg> args = new ArrayList<>();
        
        Arg pythonFilePathArg = new Arg();
        pythonFilePathArg.setName("pythonFilePath");
        pythonFilePathArg.setDescription("要执行的Python文件路径");
        pythonFilePathArg.setType("string");
        pythonFilePathArg.setRequired(true);
        args.add(pythonFilePathArg);
        
        Arg inputExcelPathArg = new Arg();
        inputExcelPathArg.setName("inputExcelPath");
        inputExcelPathArg.setDescription("输入Excel文件路径（如果有）");
        inputExcelPathArg.setType("string");
        inputExcelPathArg.setRequired(false);
        args.add(inputExcelPathArg);
        
        Arg outputExcelPathArg = new Arg();
        outputExcelPathArg.setName("outputExcelPath");
        outputExcelPathArg.setDescription("输出Excel文件保存路径");
        outputExcelPathArg.setType("string");
        outputExcelPathArg.setRequired(true);
        args.add(outputExcelPathArg);
        
        this.setArgs(args);
    }

    @Override
    public String invoke(PythonExecuteReq req) {
        try {
            // 1. 验证Python文件是否存在
            File pythonFile = new File(req.getPythonFilePath());
            if (!pythonFile.exists() || !pythonFile.isFile()) {
                return "错误: Python文件不存在 - " + req.getPythonFilePath();
            }
            
            // 2. 验证输入Excel文件是否存在（如果提供）
            if (req.getInputExcelPath() != null && !req.getInputExcelPath().isEmpty()) {
                File inputExcelFile = new File(req.getInputExcelPath());
                if (!inputExcelFile.exists() || !inputExcelFile.isFile()) {
                    return "错误: 输入Excel文件不存在 - " + req.getInputExcelPath();
                }
            }
            
            // 3. 确保输出目录存在
            Path outputPath = Paths.get(req.getOutputExcelPath());
            Files.createDirectories(outputPath.getParent());
            
            // 4. 执行Python脚本
            String result = executePythonScript(req);
            
            // 5. 检查输出文件是否生成
            File outputFile = new File(req.getOutputExcelPath());
            if (!outputFile.exists()) {
                return "Python脚本执行完毕，但输出文件未生成: " + req.getOutputExcelPath() + "\n执行结果: " + result;
            }
            
            return "Python脚本执行成功，生成Excel文件: " + req.getOutputExcelPath() + "\n" + result;
        } catch (Exception e) {
            log.error("执行Python脚本失败", e);
            return "执行Python脚本失败: " + e.getMessage();
        }
    }
    
    /**
     * 执行Python脚本
     */
    private String executePythonScript(PythonExecuteReq req) throws Exception {
        ProcessBuilder processBuilder = new ProcessBuilder();
        List<String> command = new ArrayList<>();
        
        // 添加Python解释器
        command.add(DEFAULT_PYTHON_INTERPRETER);
        
        // 添加Python脚本路径
        command.add(req.getPythonFilePath());
        
        // 添加参数
        if (req.getInputExcelPath() != null && !req.getInputExcelPath().isEmpty()) {
            command.add("--input");
            command.add(req.getInputExcelPath());
        }
        
        command.add("--output");
        command.add(req.getOutputExcelPath());
        
        processBuilder.command(command);
        
        // 捕获输出
        StringBuilder output = new StringBuilder();
        Process process = processBuilder.start();
        
        // 读取标准输出
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
        }
        
        // 读取错误输出
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                output.append("ERROR: ").append(line).append("\n");
            }
        }
        
        // 等待进程完成，带超时
        boolean completed = process.waitFor(EXECUTION_TIMEOUT, TimeUnit.SECONDS);
        if (!completed) {
            process.destroy();
            throw new Exception("Python脚本执行超时（" + EXECUTION_TIMEOUT + "秒）");
        }
        
        // 检查退出代码
        int exitCode = process.exitValue();
        if (exitCode != 0) {
            log.error("Python脚本执行失败，退出代码: {}", exitCode);
            return "Python脚本执行失败，退出代码: " + exitCode + "\n" + output.toString();
        }
        
        return output.toString();
    }
} 