package org.ruoyi.task.plugin;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.ruoyi.common.chat.openai.OpenAiClient;
import org.ruoyi.common.chat.openai.OpenAiConfig;
import org.ruoyi.common.chat.openai.OpenAiCompletionRequest;
import org.ruoyi.common.chat.openai.plugin.PluginAbstract;
import org.ruoyi.common.chat.openai.domain.ChatMessage;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Python代码生成插件
 * 基于用户需求和Excel文件生成Python代码
 */
@Slf4j
@Component
public class PythonGeneratePlugin extends PluginAbstract<PythonGenerateReq> {

    @Resource
    private OpenAiClient openAiClient;
    
    @Resource
    private OpenAiConfig openAiConfig;

    public PythonGeneratePlugin() {
        super(PythonGenerateReq.class);
        this.setName("Python代码生成插件");
        this.setFunction("generatePythonCode");
        this.setDescription("根据用户需求和Excel文件生成Python代码，用于数据处理和分析");

        List<Arg> args = new ArrayList<>();
        
        Arg requirementArg = new Arg();
        requirementArg.setName("requirement");
        requirementArg.setDescription("用户的需求描述，详细说明需要Python代码实现的功能");
        requirementArg.setType("string");
        requirementArg.setRequired(true);
        args.add(requirementArg);
        
        Arg excelFilePathArg = new Arg();
        excelFilePathArg.setName("excelFilePath");
        excelFilePathArg.setDescription("上传的Excel文件路径，Python代码将处理这个文件");
        excelFilePathArg.setType("string");
        excelFilePathArg.setRequired(true);
        args.add(excelFilePathArg);
        
        Arg outputPythonPathArg = new Arg();
        outputPythonPathArg.setName("outputPythonPath");
        outputPythonPathArg.setDescription("生成的Python文件保存路径");
        outputPythonPathArg.setType("string");
        outputPythonPathArg.setRequired(true);
        args.add(outputPythonPathArg);
        
        this.setArgs(args);
    }

    @Override
    public String invoke(PythonGenerateReq req) {
        try {
            // 1. 验证Excel文件是否存在
            File excelFile = new File(req.getExcelFilePath());
            if (!excelFile.exists() || !excelFile.isFile()) {
                return "错误: Excel文件不存在 - " + req.getExcelFilePath();
            }
            
            // 2. 读取Excel文件的前几行内容用于分析
            String excelSample = getExcelSample(req.getExcelFilePath());
            
            // 3. 构建提示信息
            String promptTemplate = getPromptTemplate();
            String prompt = promptTemplate
                    .replace("{{REQUIREMENT}}", req.getRequirement())
                    .replace("{{EXCEL_SAMPLE}}", excelSample);
            
            // 4. 调用OpenAI生成Python代码
            String pythonCode = generatePythonCodeFromAI(prompt);
            
            // 5. 提取Python代码（可能包含在Markdown代码块中）
            pythonCode = extractPythonCode(pythonCode);
            
            // 6. 保存Python代码到文件
            savePythonCode(pythonCode, req.getOutputPythonPath());
            
            return "成功生成Python代码: " + req.getOutputPythonPath();
        } catch (Exception e) {
            log.error("生成Python代码失败", e);
            return "生成Python代码失败: " + e.getMessage();
        }
    }
    
    /**
     * 从Excel文件中读取样本数据
     */
    private String getExcelSample(String excelFilePath) {
        // 这里简化处理，实际应该使用POI库读取Excel内容
        // 返回Excel文件的基本信息
        File file = new File(excelFilePath);
        return "Excel文件名: " + file.getName() + "\n" +
               "文件大小: " + (file.length() / 1024) + " KB\n" +
               "请根据用户需求和文件名分析可能的内容并生成合适的Python代码";
    }
    
    /**
     * 获取提示模板
     */
    private String getPromptTemplate() {
        try {
            // 从资源文件读取模板，如果没有可以直接使用硬编码的字符串
            ClassPathResource resource = new ClassPathResource("templates/python_generate_prompt.txt");
            if (resource.exists()) {
                return FileUtils.readFileToString(resource.getFile(), StandardCharsets.UTF_8);
            }
        } catch (IOException e) {
            log.error("读取提示模板失败", e);
        }
        
        // 默认模板
        return "请根据以下需求和Excel文件信息生成Python代码：\n\n" +
               "需求描述：\n{{REQUIREMENT}}\n\n" +
               "Excel文件信息：\n{{EXCEL_SAMPLE}}\n\n" +
               "请生成完整可执行的Python代码，包括必要的导入语句，使用pandas读取Excel文件，" +
               "进行数据处理和分析，并将结果保存为新的Excel文件。\n" +
               "代码应该具有良好的注释和错误处理。";
    }
    
    /**
     * 调用OpenAI API生成Python代码
     */
    private String generatePythonCodeFromAI(String prompt) {
        try {
            // 构建请求消息
            List<ChatMessage> messages = new ArrayList<>();
            messages.add(new ChatMessage("system", "你是一个专业的Python程序员，精通数据分析和Excel处理。" +
                    "请提供具有完整功能的Python代码，确保代码符合PEP 8标准并具有良好的注释。" +
                    "代码应包含必要的错误处理机制。"));
            messages.add(new ChatMessage("user", prompt));
            
            // 创建请求
            OpenAiCompletionRequest request = new OpenAiCompletionRequest();
            request.setModel(openAiConfig.getModel());
            request.setMessages(messages);
            request.setTemperature(0.2); // 较低的温度以获得更确定性的回答
            
            // 调用API
            String response = openAiClient.chatCompletion(request);
            return response;
        } catch (Exception e) {
            log.error("调用OpenAI生成代码失败", e);
            throw new RuntimeException("生成Python代码失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 从AI输出中提取Python代码
     */
    private String extractPythonCode(String aiOutput) {
        // 如果输出包含Markdown代码块，提取代码部分
        if (aiOutput.contains("```python")) {
            int start = aiOutput.indexOf("```python") + "```python".length();
            int end = aiOutput.indexOf("```", start);
            if (end > start) {
                return aiOutput.substring(start, end).trim();
            }
        } else if (aiOutput.contains("```")) {
            int start = aiOutput.indexOf("```") + "```".length();
            int end = aiOutput.indexOf("```", start);
            if (end > start) {
                return aiOutput.substring(start, end).trim();
            }
        }
        
        // 如果没有代码块标记，返回整个响应
        return aiOutput;
    }
    
    /**
     * 保存Python代码到文件
     */
    private void savePythonCode(String code, String outputPath) throws IOException {
        // 确保目录存在
        Path path = Paths.get(outputPath);
        Files.createDirectories(path.getParent());
        
        // 写入文件
        Files.write(path, code.getBytes(StandardCharsets.UTF_8));
        log.info("Python代码已保存到: {}", outputPath);
    }
} 