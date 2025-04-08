package org.ruoyi.task.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Python执行配置类
 */
@Configuration
@ConfigurationProperties(prefix = "python.execution")
public class PythonExecutionConfig {
    
    /**
     * Python解释器路径
     */
    private String pythonPath = "python";
    
    /**
     * 执行超时时间（秒）
     */
    private int timeoutSeconds = 300;
    
    /**
     * 最大内存使用（MB）
     */
    private int maxMemoryMb = 1024;
    
    /**
     * 临时文件目录
     */
    private String tempDir = System.getProperty("java.io.tmpdir");
    
    /**
     * 是否启用虚拟环境
     */
    private boolean useVirtualEnv = false;
    
    /**
     * 虚拟环境路径
     */
    private String virtualEnvPath;
    
    public String getPythonPath() {
        return pythonPath;
    }
    
    public void setPythonPath(String pythonPath) {
        this.pythonPath = pythonPath;
    }
    
    public int getTimeoutSeconds() {
        return timeoutSeconds;
    }
    
    public void setTimeoutSeconds(int timeoutSeconds) {
        this.timeoutSeconds = timeoutSeconds;
    }
    
    public int getMaxMemoryMb() {
        return maxMemoryMb;
    }
    
    public void setMaxMemoryMb(int maxMemoryMb) {
        this.maxMemoryMb = maxMemoryMb;
    }
    
    public String getTempDir() {
        return tempDir;
    }
    
    public void setTempDir(String tempDir) {
        this.tempDir = tempDir;
    }
    
    public boolean isUseVirtualEnv() {
        return useVirtualEnv;
    }
    
    public void setUseVirtualEnv(boolean useVirtualEnv) {
        this.useVirtualEnv = useVirtualEnv;
    }
    
    public String getVirtualEnvPath() {
        return virtualEnvPath;
    }
    
    public void setVirtualEnvPath(String virtualEnvPath) {
        this.virtualEnvPath = virtualEnvPath;
    }
} 