package org.ruoyi.task.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.ruoyi.task.service.FileStorageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * 文件存储服务实现类
 */
@Slf4j
@Service
public class FileStorageServiceImpl implements FileStorageService {

    @Value("${task.upload.dir:${user.home}/ruoyi-ai/uploads}")
    private String uploadDir;
    
    private Path fileStorageLocation;
    
    @PostConstruct
    public void init() {
        fileStorageLocation = Paths.get(uploadDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(fileStorageLocation);
            log.info("文件存储目录已初始化: {}", fileStorageLocation);
        } catch (Exception ex) {
            log.error("无法创建文件存储目录: {}", fileStorageLocation, ex);
            throw new RuntimeException("无法创建文件存储目录", ex);
        }
    }
    
    @Override
    public String storeFile(MultipartFile file, String directoryName) throws IOException {
        // 检查文件名
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            throw new IOException("无效的文件名");
        }
        
        // 清理文件名
        String filename = StringUtils.cleanPath(originalFilename);
        
        // 检查文件名是否包含无效字符
        if (filename.contains("..")) {
            throw new IOException("文件名包含无效路径序列: " + filename);
        }
        
        // 生成存储文件名（避免重名）
        String uniqueFilename = generateUniqueFilename(filename);
        
        // 创建存储路径
        Path targetLocation = createStoragePath(directoryName).resolve(uniqueFilename);
        
        // 存储文件
        Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
        
        log.info("文件已存储: {}", targetLocation);
        return targetLocation.toString();
    }
    
    @Override
    public String storeGeneratedFile(String content, String filename, String directoryName) throws IOException {
        // 清理文件名
        String cleanFilename = StringUtils.cleanPath(filename);
        
        // 检查文件名是否包含无效字符
        if (cleanFilename.contains("..")) {
            throw new IOException("文件名包含无效路径序列: " + cleanFilename);
        }
        
        // 生成存储文件名（避免重名）
        String uniqueFilename = generateUniqueFilename(cleanFilename);
        
        // 创建存储路径
        Path targetLocation = createStoragePath(directoryName).resolve(uniqueFilename);
        
        // 写入文件内容
        Files.write(targetLocation, content.getBytes(StandardCharsets.UTF_8));
        
        log.info("生成的文件已存储: {}", targetLocation);
        return targetLocation.toString();
    }
    
    @Override
    public Path getFilePath(String filename, String directoryName) {
        return createStoragePath(directoryName).resolve(filename);
    }
    
    @Override
    public String readFileContent(String filepath) throws IOException {
        Path path = Paths.get(filepath);
        return new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
    }
    
    @Override
    public boolean fileExists(String filepath) {
        Path path = Paths.get(filepath);
        return Files.exists(path);
    }
    
    @Override
    public String getUploadRootDir() {
        return fileStorageLocation.toString();
    }
    
    /**
     * 创建存储路径
     * 如果指定了目录名，则使用指定的目录，否则使用日期格式的目录
     */
    private Path createStoragePath(String directoryName) {
        Path storagePath;
        
        if (directoryName != null && !directoryName.isEmpty()) {
            storagePath = fileStorageLocation.resolve(directoryName);
        } else {
            // 使用日期作为子目录
            String dateDir = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            storagePath = fileStorageLocation.resolve(dateDir);
        }
        
        try {
            Files.createDirectories(storagePath);
        } catch (IOException e) {
            log.error("无法创建存储目录: {}", storagePath, e);
            throw new RuntimeException("无法创建存储目录", e);
        }
        
        return storagePath;
    }
    
    /**
     * 生成唯一文件名，避免文件重名
     */
    private String generateUniqueFilename(String originalFilename) {
        String extension = "";
        int lastDotIndex = originalFilename.lastIndexOf('.');
        
        if (lastDotIndex > 0) {
            extension = originalFilename.substring(lastDotIndex);
            originalFilename = originalFilename.substring(0, lastDotIndex);
        }
        
        return originalFilename + "_" + UUID.randomUUID().toString().substring(0, 8) + extension;
    }
} 