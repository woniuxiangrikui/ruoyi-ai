package org.ruoyi.task.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ruoyi.common.chat.entity.chat.*;
import org.ruoyi.common.chat.openai.OpenAiClient;
import org.ruoyi.common.chat.openai.OpenAiCompletionRequest;
import org.ruoyi.common.chat.openai.domain.ChatMessage;
import org.ruoyi.task.core.PluginRegistryService;
import org.ruoyi.task.service.AiService;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

/**
 * AI服务实现类
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AiServiceImpl implements AiService {
    
    private final OpenAiClient openAiClient;
    private final PluginRegistryService pluginRegistry;
    
    @Override
    public String generateTaskPlan(String prompt) {
        try {
            log.info("生成任务计划，提示长度: {}", prompt.length());
            
            // 构建请求
            OpenAiCompletionRequest request = new OpenAiCompletionRequest();
            request.setModel("gpt-4");
            request.setMessages(Collections.singletonList(
                new ChatMessage("user", prompt)
            ));
            
            // 调用API
            String response = openAiClient.chatCompletion(request);
            
            // 解析结果
            log.info("任务计划生成成功，长度: {}", response.length());
            return response;
        } catch (Exception e) {
            log.error("任务计划生成失败", e);
            throw new RuntimeException("任务计划生成失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 模拟任务计划生成的结果（测试用）
     */
    public String simulateTaskPlanGeneration(String prompt) {
        // 如果包含Excel列交换请求
        if (prompt.toLowerCase().contains("excel") && prompt.contains("交换")) {
            return "{\n" +
                   "  \"analysis\": \"用户需要处理Excel文件，首先交换第一列和第二列，然后按照第三列进行分组统计\",\n" +
                   "  \"tasks\": [\n" +
                   "    {\n" +
                   "      \"id\": \"task1\",\n" +
                   "      \"description\": \"交换Excel文件的第一列和第二列\",\n" +
                   "      \"inputFile\": \"a.xlsx\",\n" +
                   "      \"outputFile\": \"temp_swapped.xlsx\",\n" +
                   "      \"usePlugin\": true,\n" +
                   "      \"pluginName\": \"Python代码生成插件\",\n" +
                   "      \"pluginParameters\": {\n" +
                   "        \"filePath\": \"a.xlsx\",\n" +
                   "        \"analysisType\": \"数据转换\",\n" +
                   "        \"outputFormat\": \"excel\",\n" +
                   "        \"additionalRequirements\": \"交换第一列和第二列，保存为temp_swapped.xlsx\"\n" +
                   "      }\n" +
                   "    },\n" +
                   "    {\n" +
                   "      \"id\": \"task2\",\n" +
                   "      \"description\": \"按照第三列进行分组统计\",\n" +
                   "      \"inputFile\": \"temp_swapped.xlsx\",\n" +
                   "      \"outputFile\": \"分组统计结果.xlsx\",\n" +
                   "      \"usePlugin\": true,\n" +
                   "      \"pluginName\": \"Python代码生成插件\",\n" +
                   "      \"pluginParameters\": {\n" +
                   "        \"filePath\": \"temp_swapped.xlsx\",\n" +
                   "        \"analysisType\": \"数据统计\",\n" +
                   "        \"outputFormat\": \"excel\",\n" +
                   "        \"additionalRequirements\": \"按第三列分组统计，生成统计结果和可视化\"\n" +
                   "      }\n" +
                   "    }\n" +
                   "  ]\n" +
                   "}";
        }
        
        // 默认任务计划
        return "{\n" +
               "  \"analysis\": \"用户需要处理文件并生成报告\",\n" +
               "  \"tasks\": [\n" +
               "    {\n" +
               "      \"id\": \"task1\",\n" +
               "      \"description\": \"处理Excel文件数据\",\n" +
               "      \"inputFile\": \"input.xlsx\",\n" +
               "      \"outputFile\": \"output.xlsx\",\n" +
               "      \"usePlugin\": true,\n" +
               "      \"pluginName\": \"Python代码生成插件\",\n" +
               "      \"pluginParameters\": {\n" +
               "        \"filePath\": \"input.xlsx\",\n" +
               "        \"analysisType\": \"数据处理\",\n" +
               "        \"outputFormat\": \"excel\",\n" +
               "        \"additionalRequirements\": \"根据需求处理数据\"\n" +
               "      }\n" +
               "    }\n" +
               "  ]\n" +
               "}";
    }
} 