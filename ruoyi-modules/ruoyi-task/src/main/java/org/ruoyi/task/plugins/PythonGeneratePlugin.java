package org.ruoyi.task.plugins;

import lombok.extern.slf4j.Slf4j;
import org.ruoyi.common.chat.openai.OpenAiClient;
import org.ruoyi.common.chat.openai.OpenAiCompletionRequest;
import org.ruoyi.common.chat.openai.domain.ChatMessage;
import org.ruoyi.task.core.PluginAbstract;
import org.ruoyi.task.domain.PythonGenerateReq;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Python代码生成插件
 */
@Slf4j
@Component
public class PythonGeneratePlugin extends PluginAbstract<PythonGenerateReq> {

    @Autowired
    private OpenAiClient openAiClient;
    
    /**
     * 构造函数
     */
    public PythonGeneratePlugin() {
        super(PythonGenerateReq.class, "python_generation", "数据处理与分析 - 使用Python处理Excel等数据文件");
        this.setName("Python代码生成插件");
        this.setFunction("generatePythonCode");
        this.setDescription("根据用户需求生成Python代码，用于数据处理和分析");
        
        // 定义参数
        Arg filePathArg = new Arg();
        filePathArg.setName("filePath");
        filePathArg.setDescription("要处理的文件路径");
        filePathArg.setType("string");
        filePathArg.setRequired(true);
        
        Arg analysisTypeArg = new Arg();
        analysisTypeArg.setName("analysisType");
        analysisTypeArg.setDescription("分析类型");
        analysisTypeArg.setType("string");
        analysisTypeArg.setRequired(true);
        
        Arg outputFormatArg = new Arg();
        outputFormatArg.setName("outputFormat");
        outputFormatArg.setDescription("输出格式");
        outputFormatArg.setType("string");
        outputFormatArg.setRequired(false);
        
        Arg additionalRequirementsArg = new Arg();
        additionalRequirementsArg.setName("additionalRequirements");
        additionalRequirementsArg.setDescription("其他处理要求");
        additionalRequirementsArg.setType("string");
        additionalRequirementsArg.setRequired(false);
        
        this.setArgs(Arrays.asList(filePathArg, analysisTypeArg, outputFormatArg, additionalRequirementsArg));
    }
    
    /**
     * 生成Python代码
     */
    public String generatePythonCode(PythonGenerateReq req) {
        try {
            // 构建提示
            String prompt = buildPythonCodePrompt(req);
            
            // 调用AI模型生成代码
            String pythonCode = callLLM(prompt);
            
            log.info("生成Python代码成功，长度: {}", pythonCode.length());
            return pythonCode;
        } catch (Exception e) {
            log.error("生成Python代码失败", e);
            throw new RuntimeException("生成Python代码失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 构建Python代码生成提示
     */
    private String buildPythonCodePrompt(PythonGenerateReq req) {
        StringBuilder prompt = new StringBuilder();
        
        prompt.append("生成一个Python脚本，用于处理以下任务：\n\n");
        
        // 添加文件信息
        prompt.append("文件路径: ").append(req.getFilePath()).append("\n");
        
        // 添加分析类型
        prompt.append("分析类型: ").append(req.getAnalysisType()).append("\n");
        
        // 添加输出格式（如果有）
        if (req.getOutputFormat() != null && !req.getOutputFormat().isEmpty()) {
            prompt.append("输出格式: ").append(req.getOutputFormat()).append("\n");
        }
        
        // 添加额外需求（如果有）
        if (req.getAdditionalRequirements() != null && !req.getAdditionalRequirements().isEmpty()) {
            prompt.append("\n具体要求: ").append(req.getAdditionalRequirements()).append("\n");
        }
        
        prompt.append("\n生成的代码应该完整、可运行，并包含适当的注释。");
        prompt.append("\n使用pandas库处理Excel文件，并妥善处理可能的错误情况。");
        prompt.append("\n如果需要进行数据分析，请使用pandas和numpy相关函数。");
        prompt.append("\n如果需要可视化，请使用matplotlib或seaborn生成图表。");
        
        return prompt.toString();
    }
    
    /**
     * 调用大模型生成代码
     */
    private String callLLM(String prompt) {
        // 构建请求
        OpenAiCompletionRequest request = new OpenAiCompletionRequest();
        request.setModel("gpt-4");  // 可设置为配置项
        
        List<ChatMessage> messages = Arrays.asList(
            new ChatMessage("system", "你是一个Python编程专家，尤其擅长使用pandas和numpy处理数据。请根据需求生成完整、可执行的Python代码。代码中应包含适当的注释和错误处理。"),
            new ChatMessage("user", prompt)
        );
        request.setMessages(messages);
        
        // 调用API
        return openAiClient.chatCompletion(request);
    }
} 