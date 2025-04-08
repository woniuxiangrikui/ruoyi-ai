package org.ruoyi.task.core;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ruoyi.common.chat.openai.plugin.PluginAbstract;
import org.ruoyi.common.chat.openai.plugin.PluginDefinition;
import org.ruoyi.task.service.AiService;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 任务计划生成器
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class TaskPlanGenerator {

    private final PluginRegistryService pluginRegistry;
    private final AiService aiService;
    private final ObjectMapper objectMapper;
    
    /**
     * 根据用户输入生成任务计划
     */
    public String generateTaskPlan(String userInput, String filePath) {
        try {
            // 获取所有可用插件
            List<PluginDefinition> availablePlugins = pluginRegistry.getAllPlugins();
            
            // 构建任务计划提示
            String prompt = buildTaskPlanPrompt(userInput, filePath, availablePlugins);
            
            // 调用AI服务生成任务计划JSON
            return aiService.generateTaskPlan(prompt);
        } catch (Exception e) {
            log.error("生成任务计划失败", e);
            throw new RuntimeException("任务计划生成失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 构建任务计划提示
     */
    private String buildTaskPlanPrompt(String userInput, String filePath, List<PluginDefinition> availablePlugins) {
        StringBuilder prompt = new StringBuilder();
        
        prompt.append("# Excel文件数据处理任务拆解\n\n");
        
        prompt.append("## 用户需求\n");
        prompt.append(userInput).append("\n\n");
        
        prompt.append("## 文件信息\n");
        prompt.append("Excel文件路径: ").append(filePath).append("\n\n");
        
        prompt.append("## 可用插件列表\n");
        for (PluginDefinition plugin : availablePlugins) {
            prompt.append("- ").append(plugin.getName()).append(": ").append(plugin.getDescription()).append("\n");
            prompt.append("  功能: ").append(plugin.getFunction()).append("\n");
            // 添加插件参数信息
            prompt.append("  参数: ").append(getPluginParametersDescription(plugin)).append("\n\n");
        }
        
        prompt.append("## 任务拆分要求\n");
        prompt.append("请将用户的数据处理需求拆分为多个独立的任务，每个任务负责完成一个具体操作。要求：\n");
        prompt.append("1. 每个任务必须生成一个Excel文件作为输出\n");
        prompt.append("2. 后续任务应使用前序任务的输出文件作为输入\n");
        prompt.append("3. 任务之间保持连续性和依赖关系\n");
        prompt.append("4. 如果需要调用插件，明确指定插件名称和所需参数\n\n");
        
        prompt.append("## 输出格式\n");
        prompt.append("```json\n");
        prompt.append("{\n");
        prompt.append("  \"analysis\": \"对用户需求的分析\",\n");
        prompt.append("  \"tasks\": [\n");
        prompt.append("    {\n");
        prompt.append("      \"id\": \"task1\",\n");
        prompt.append("      \"description\": \"任务描述\",\n");
        prompt.append("      \"inputFile\": \"输入文件路径\",\n");
        prompt.append("      \"outputFile\": \"输出文件路径\",\n");
        prompt.append("      \"usePlugin\": true,\n");
        prompt.append("      \"pluginName\": \"Python代码生成插件\",\n");
        prompt.append("      \"pluginParameters\": {\n");
        prompt.append("        \"filePath\": \"输入文件路径\",\n");
        prompt.append("        \"analysisType\": \"数据转换\",\n");
        prompt.append("        \"outputFormat\": \"excel\",\n");
        prompt.append("        \"additionalRequirements\": \"具体处理要求\"\n");
        prompt.append("      }\n");
        prompt.append("    }\n");
        prompt.append("  ]\n");
        prompt.append("}\n");
        prompt.append("```\n");
        
        return prompt.toString();
    }
    
    /**
     * 获取插件参数描述
     */
    private String getPluginParametersDescription(PluginDefinition plugin) {
        StringBuilder description = new StringBuilder();
        
        List<PluginAbstract.Arg> args = plugin.getArgs();
        if (args != null && !args.isEmpty()) {
            for (PluginAbstract.Arg arg : args) {
                description.append(arg.getName())
                      .append("(").append(arg.getType()).append(")")
                      .append(": ").append(arg.getDescription())
                      .append(arg.isRequired() ? " (必填)" : " (可选)")
                      .append("; ");
            }
        }
        
        return description.toString();
    }
}