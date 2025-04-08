package org.ruoyi.task.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Path;

/**
 * 文件存储服务接口
 */
public interface FileStorageService {

    /**
     * 存储上传的文件
     *
     * @param file 上传的文件
     * @param directoryName 保存目录名（可选）
     * @return 文件保存的绝对路径
     * @throws IOException 如果存储过程中发生IO错误
     */
    String storeFile(MultipartFile file, String directoryName) throws IOException;

    /**
     * 存储生成的文件
     *
     * @param content 文件内容
     * @param filename 文件名
     * @param directoryName 保存目录名（可选）
     * @return 文件保存的绝对路径
     * @throws IOException 如果存储过程中发生IO错误
     */
    String storeGeneratedFile(String content, String filename, String directoryName) throws IOException;

    /**
     * 获取文件的绝对路径
     *
     * @param filename 文件名
     * @param directoryName 文件所在目录名（可选）
     * @return 文件的绝对路径
     */
    Path getFilePath(String filename, String directoryName);

    /**
     * 读取文件内容
     *
     * @param filepath 文件路径
     * @return 文件内容
     * @throws IOException 如果读取过程中发生IO错误
     */
    String readFileContent(String filepath) throws IOException;

    /**
     * 检查文件是否存在
     *
     * @param filepath 文件路径
     * @return 如果文件存在则返回true，否则返回false
     */
    boolean fileExists(String filepath);
    
    /**
     * 获取上传目录的根路径
     *
     * @return 上传目录的根路径
     */
    String getUploadRootDir();
} 