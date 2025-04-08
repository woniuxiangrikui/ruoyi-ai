package org.ruoyi.task.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 文件上传响应对象
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class FileResponse {
    
    /**
     * 是否成功
     */
    private boolean success;
    
    /**
     * 消息
     */
    private String message;
    
    /**
     * 文件信息
     */
    private FileInfo fileInfo;
    
    /**
     * 文件信息
     */
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class FileInfo {
        /**
         * 文件名
         */
        private String fileName;
        
        /**
         * 文件下载URI
         */
        private String fileDownloadUri;
        
        /**
         * 文件类型
         */
        private String fileType;
        
        /**
         * 文件大小
         */
        private long size;
        
        /**
         * 文件路径
         */
        private String filePath;
    }
} 