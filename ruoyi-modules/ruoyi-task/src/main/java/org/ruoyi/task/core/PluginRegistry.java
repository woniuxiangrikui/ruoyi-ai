package org.ruoyi.task.core;

import org.ruoyi.common.chat.openai.plugin.PluginAbstract;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class PluginRegistry {
    private final Map<String, PluginAbstract<?, ?>> plugins = new HashMap<>();

    /**
     * 注册插件
     */
    public void registerPlugin(PluginAbstract<?, ?> plugin) {
        plugins.put(plugin.getFunction(), plugin);
    }

    /**
     * 获取所有插件
     */
    public List<PluginAbstract<?, ?>> getAllPlugins() {
        return new ArrayList<>(plugins.values());
    }

    /**
     * 获取指定插件
     */
    public PluginAbstract<?, ?> getPlugin(String functionName) {
        return plugins.get(functionName);
    }

    /**
     * 获取所有插件函数定义
     */
    public List<Map<String, Object>> getAllPluginDefinitions() {
        List<Map<String, Object>> definitions = new ArrayList<>();
        
        for (PluginAbstract<?, ?> plugin : plugins.values()) {
            Map<String, Object> definition = new HashMap<>();
            definition.put("name", plugin.getFunction());
            definition.put("description", plugin.getDescription());
            definition.put("parameters", plugin.getParameters());
            definitions.add(definition);
        }
        
        return definitions;
    }
} 