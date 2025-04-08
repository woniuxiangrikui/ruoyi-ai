package org.ruoyi.task.core;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

/**
 * 插件抽象基类
 */
@Slf4j
public abstract class PluginAbstract<T> implements PluginDefinition {
    
    @Getter
    private final Class<T> paramClass;
    
    @Getter
    @Setter
    private String name;
    
    @Getter
    @Setter
    private String function;
    
    @Getter
    @Setter
    private String description;
    
    @Getter
    @Setter
    private List<Arg> args;
    
    // 分类信息
    @Getter
    private final String category;
    
    @Getter
    private final String categoryDescription;
    
    /**
     * 构造函数
     */
    protected PluginAbstract(Class<T> paramClass, String category, String categoryDescription) {
        this.paramClass = paramClass;
        this.category = category;
        this.categoryDescription = categoryDescription;
    }
} 