package org.ruoyi.task.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ruoyi.common.chat.openai.plugin.PluginAbstract;
import org.ruoyi.common.chat.plugin.CmdPlugin;
import org.ruoyi.common.chat.plugin.CmdReq;
import org.ruoyi.common.chat.plugin.SqlPlugin;
import org.ruoyi.common.chat.plugin.SqlReq;
import org.ruoyi.task.core.PluginRegistry;
import org.ruoyi.task.core.TaskExecutor;
import org.ruoyi.task.core.TaskPlanGenerator;
import org.ruoyi.task.domain.TaskExecutionResult;
import org.ruoyi.task.domain.TaskPlan;
import org.ruoyi.task.listener.SseTaskExecutionListener;
import org.ruoyi.task.service.ITaskService;
import org.ruoyi.task.utils.SseEmitterUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class TaskServiceImpl implements ITaskService {
    
    private final PluginRegistry pluginRegistry;
    private final TaskPlanGenerator taskPlanGenerator;
    private final TaskExecutor taskExecutor;
    
    @PostConstruct
    public void init() {
        // 注册所有可用插件
        registerPlugins();
    }
    
    private void registerPlugins() {
        // 注册SQL插件
        SqlPlugin sqlPlugin = new SqlPlugin(SqlReq.class);
        sqlPlugin.setName("数据库查询插件");
        sqlPlugin.setFunction("sqlPlugin");
        sqlPlugin.setDescription("提供一个用户名称查询余额信息");
        
        PluginAbstract.Arg sqlArg = new PluginAbstract.Arg();
        sqlArg.setName("username");
        sqlArg.setDescription("用户名称");
        sqlArg.setType("string");
        sqlArg.setRequired(true);
        sqlPlugin.setArgs(Collections.singletonList(sqlArg));
        
        // 注册命令行插件
        CmdPlugin cmdPlugin = new CmdPlugin(CmdReq.class);
        cmdPlugin.setName("命令行工具");
        cmdPlugin.setFunction("openCmd");
        cmdPlugin.setDescription("提供一个命令行指令,比如<记事本>,指令使用中文");
        
        PluginAbstract.Arg cmdArg = new PluginAbstract.Arg();
        cmdArg.setName("cmd");
        cmdArg.setDescription("命令行指令");
        cmdArg.setType("string");
        cmdArg.setRequired(true);
        cmdPlugin.setArgs(Collections.singletonList(cmdArg));
        
        // 注册到注册表
        pluginRegistry.registerPlugin(sqlPlugin);
        pluginRegistry.registerPlugin(cmdPlugin);
        
        // 可以添加更多插件...
    }
    
    @Override
    public TaskExecutionResult executeComplexTask(String userInput) {
        try {
            log.info("开始处理复杂任务: {}", userInput);
            
            // 生成任务计划
            TaskPlan taskPlan = taskPlanGenerator.generateTaskPlan(
                userInput, 
                pluginRegistry.getAllPluginDefinitions()
            );
            
            log.info("生成的任务计划: {}", taskPlan);
            
            // 执行任务
            String result = taskExecutor.executeTasks(taskPlan, userInput);
            
            // 返回执行结果
            TaskExecutionResult executionResult = new TaskExecutionResult();
            executionResult.setUserInput(userInput);
            executionResult.setAnalysis(taskPlan.getAnalysis());
            executionResult.setTasks(taskPlan.getTasks());
            executionResult.setResult(result);
            
            return executionResult;
            
        } catch (Exception e) {
            log.error("复杂任务执行失败", e);
            throw new RuntimeException("任务执行失败: " + e.getMessage());
        }
    }
    
    /**
     * 异步执行复杂任务，支持实时进度推送
     */
    @Override
    public SseEmitter executeComplexTaskAsync(String userInput) {
        SseEmitter emitter = new SseEmitter(-1L); // 无超时
        
        // 创建SSE任务执行监听器
        SseTaskExecutionListener listener = new SseTaskExecutionListener(emitter);
        
        // 使用异步线程执行任务
        CompletableFuture.runAsync(() -> {
            try {
                log.info("开始处理异步复杂任务: {}", userInput);
                
                // 生成任务计划
                TaskPlan taskPlan = taskPlanGenerator.generateTaskPlan(
                    userInput, 
                    pluginRegistry.getAllPluginDefinitions()
                );
                
                log.info("生成的任务计划: {}", taskPlan);
                
                // 执行任务，传入监听器
                taskExecutor.executeTasks(taskPlan, userInput, listener);
            } catch (Exception e) {
                log.error("复杂任务执行失败", e);
                SseEmitterUtils.sendErrorEvent(emitter, "任务执行失败: " + e.getMessage());
                SseEmitterUtils.complete(emitter);
            }
        });
        
        // 设置超时或错误回调
        emitter.onTimeout(() -> SseEmitterUtils.complete(emitter));
        emitter.onError((e) -> {
            log.error("SSE错误", e);
            SseEmitterUtils.complete(emitter);
        });
        
        return emitter;
    }
} 