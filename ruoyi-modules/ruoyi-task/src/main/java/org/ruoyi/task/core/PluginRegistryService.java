package org.ruoyi.task.core;

import lombok.extern.slf4j.Slf4j;
import org.ruoyi.common.chat.openai.plugin.PluginAbstract;
import org.ruoyi.common.chat.openai.plugin.PluginDefinition;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 插件注册服务
 */
@Service
@Slf4j
public class PluginRegistryService {

    // 按名称存储插件定义
    private final Map<String, PluginDefinition> pluginDefinitions = new ConcurrentHashMap<>();
    
    // 按类别分组的插件
    private final Map<String, List<PluginDefinition>> pluginsByCategory = new ConcurrentHashMap<>();
    
    /**
     * 注册插件
     */
    public void registerPlugin(PluginDefinition plugin) {
        pluginDefinitions.put(plugin.getName(), plugin);
        
        // 如果是抽象插件，添加到类别映射
        if (plugin instanceof PluginAbstract) {
            PluginAbstract<?> abstractPlugin = (PluginAbstract<?>) plugin;
            String category = abstractPlugin.getCategory();
            
            pluginsByCategory.computeIfAbsent(category, k -> new ArrayList<>())
                            .add(plugin);
            
            log.info("已注册插件 [{}] 到类别 [{}]", plugin.getName(), category);
        } else {
            log.info("已注册插件 [{}]", plugin.getName());
        }
    }
    
    /**
     * 获取所有插件定义
     */
    public List<PluginDefinition> getAllPluginDefinitions() {
        return new ArrayList<>(pluginDefinitions.values());
    }
    
    /**
     * 获取所有插件（排除cmd相关插件）
     */
    public List<PluginDefinition> getAllPlugins() {
        // 只返回Python相关插件，不包含cmd plugin
        return pluginDefinitions.values().stream()
            .filter(plugin -> !plugin.getName().contains("命令") && 
                              !plugin.getFunction().contains("cmd") &&
                              !plugin.getFunction().contains("Cmd"))
            .collect(Collectors.toList());
    }
    
    /**
     * 按类别获取插件
     */
    public List<PluginDefinition> getPluginsByCategory(String category) {
        return pluginsByCategory.getOrDefault(category, Collections.emptyList());
    }
    
    /**
     * 获取所有可用的插件类别
     */
    public Set<String> getAllCategories() {
        return pluginsByCategory.keySet();
    }
    
    /**
     * 获取所有类别及其描述
     */
    public Map<String, String> getCategoryDescriptions() {
        Map<String, String> descriptions = new HashMap<>();
        
        for (String category : getAllCategories()) {
            List<PluginDefinition> plugins = getPluginsByCategory(category);
            if (!plugins.isEmpty() && plugins.get(0) instanceof PluginAbstract) {
                PluginAbstract<?> plugin = (PluginAbstract<?>) plugins.get(0);
                descriptions.put(category, plugin.getCategoryDescription());
            }
        }
        
        return descriptions;
    }
    
    /**
     * 获取插件实例
     */
    public PluginDefinition getPlugin(String name) {
        return pluginDefinitions.get(name);
    }
} 