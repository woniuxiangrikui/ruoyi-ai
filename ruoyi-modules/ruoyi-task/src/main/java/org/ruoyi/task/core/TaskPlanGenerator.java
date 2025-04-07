package org.ruoyi.task.core;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ruoyi.common.chat.entity.chat.*;
import org.ruoyi.common.chat.openai.OpenAiClient;
import org.ruoyi.task.domain.TaskPlan;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class TaskPlanGenerator {
    private final OpenAiClient openAiClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 生成任务执行计划
     */
    public TaskPlan generateTaskPlan(String userInput, List<Map<String, Object>> availablePlugins) {
        try {
            // 构建系统提示词
            String systemPrompt = "你是一个任务分析助手，需要分析用户的需求并拆解成多个子任务。每个子任务应该指定使用哪个函数来完成。\n"
                + "可用的函数有：\n" + objectMapper.writeValueAsString(availablePlugins) + "\n"
                + "你需要输出一个JSON格式的任务计划，格式如下：\n"
                + "{\n"
                + "  \"analysis\": \"对用户需求的整体分析\",\n"
                + "  \"tasks\": [\n"
                + "    {\n"
                + "      \"taskId\": 1,\n"
                + "      \"description\": \"任务描述\",\n"
                + "      \"functionName\": \"要调用的函数名\",\n"
                + "      \"dependsOn\": [前置任务ID] 或 null\n"
                + "    }\n"
                + "  ]\n"
                + "}";
            
            // 构建消息
            List<Message> messages = new ArrayList<>();
            messages.add(Message.builder().role(Message.Role.SYSTEM).content(systemPrompt).build());
            messages.add(Message.builder().role(Message.Role.USER).content(userInput).build());
            
            // 设置响应格式为JSON
            ChatCompletion chatCompletion = ChatCompletion.builder()
                .messages(messages)
                .model("gpt-4-turbo")
                .build();
                
            if (ResponseFormat.Type.JSON_OBJECT != null) {
                chatCompletion.setResponseFormat(
                    ResponseFormat.builder().type(ResponseFormat.Type.JSON_OBJECT.getName()).build()
                );
            }
                
            // 调用模型
            ChatCompletionResponse response = openAiClient.chatCompletion(chatCompletion);
            String planJson = response.getChoices().get(0).getMessage().getContent();
            
            // 解析JSON为TaskPlan对象
            return objectMapper.readValue(planJson, TaskPlan.class);
            
        } catch (Exception e) {
            log.error("生成任务计划失败", e);
            throw new RuntimeException("生成任务计划失败: " + e.getMessage());
        }
    }
} 