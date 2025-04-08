package org.ruoyi.task.core;

import lombok.RequiredArgsConstructor;
import org.ruoyi.common.chat.openai.plugin.PluginDefinition;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 插件选择器
 */
@Component
@RequiredArgsConstructor
public class PluginSelector {
    
    private final PluginRegistryService pluginRegistry;
    
    /**
     * 根据意图选择适用的插件
     */
    public List<PluginDefinition> selectPluginsForIntent(String intent) {
        // 直接获取所有插件，不再依赖VectorStore
        return pluginRegistry.getAllPlugins();
    }
} 