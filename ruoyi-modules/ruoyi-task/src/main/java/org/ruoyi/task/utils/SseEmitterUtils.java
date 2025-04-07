package org.ruoyi.task.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.ruoyi.task.domain.TaskProgressEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * SSE Emitter 工具类
 */
public class SseEmitterUtils {
    private static final Logger log = LoggerFactory.getLogger(SseEmitterUtils.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 发送任务进度事件
     * 
     * @param emitter SSE发射器
     * @param event 任务进度事件
     * @return 发送是否成功
     */
    public static boolean sendTaskProgressEvent(SseEmitter emitter, TaskProgressEvent event) {
        try {
            String eventData = objectMapper.writeValueAsString(event);
            emitter.send(SseEmitter.event()
                    .name("taskProgress")
                    .data(eventData));
            return true;
        } catch (JsonProcessingException e) {
            log.error("任务进度事件序列化失败", e);
            return false;
        } catch (Exception e) {
            log.error("发送任务进度事件失败", e);
            return false;
        }
    }

    /**
     * 发送错误事件
     * 
     * @param emitter SSE发射器
     * @param message 错误消息
     * @return 发送是否成功
     */
    public static boolean sendErrorEvent(SseEmitter emitter, String message) {
        try {
            emitter.send(SseEmitter.event()
                    .name("error")
                    .data(message));
            return true;
        } catch (Exception e) {
            log.error("发送错误事件失败", e);
            return false;
        }
    }

    /**
     * 完成SSE流
     * 
     * @param emitter SSE发射器
     */
    public static void complete(SseEmitter emitter) {
        try {
            emitter.complete();
        } catch (Exception e) {
            log.error("完成SSE流失败", e);
        }
    }
} 