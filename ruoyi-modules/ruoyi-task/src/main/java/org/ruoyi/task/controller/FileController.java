package org.ruoyi.task.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ruoyi.task.domain.FileResponse;
import org.ruoyi.task.service.FileStorageService;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 文件上传和下载控制器
 */
@Slf4j
@RestController
@RequestMapping("/system/file")
@RequiredArgsConstructor
public class FileController {

    private final FileStorageService fileStorageService;
    
    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList("xlsx", "xls", "csv");

    /**
     * 上传单个文件
     */
    @PostMapping("/upload")
    public FileResponse uploadFile(@RequestParam("file") MultipartFile file, 
                                  @RequestParam(value = "directory", required = false) String directory) {
        log.info("接收到文件上传请求: {}, 目录: {}", file.getOriginalFilename(), directory);
        
        // 验证文件扩展名
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            return new FileResponse(false, "无效的文件名", null);
        }
        
        String extension = getFileExtension(originalFilename);
        if (extension == null || !ALLOWED_EXTENSIONS.contains(extension.toLowerCase())) {
            return new FileResponse(false, "只允许上传Excel文件 (.xlsx, .xls, .csv)", null);
        }
        
        try {
            // 存储文件
            String filePath = fileStorageService.storeFile(file, directory);
            
            // 生成文件下载URI
            String fileDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath()
                    .path("/system/file/download/")
                    .path(Paths.get(filePath).getFileName().toString())
                    .toUriString();
            
            return new FileResponse(true, "文件上传成功", 
                    new FileResponse.FileInfo(
                            Paths.get(filePath).getFileName().toString(),
                            fileDownloadUri,
                            file.getContentType(),
                            file.getSize(),
                            filePath
                    ));
        } catch (IOException ex) {
            log.error("文件上传失败", ex);
            return new FileResponse(false, "文件上传失败: " + ex.getMessage(), null);
        }
    }
    
    /**
     * 下载文件
     */
    @GetMapping("/download/{filename:.+}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String filename,
                                               @RequestParam(value = "directory", required = false) String directory) {
        try {
            // 获取文件路径
            Path filePath = fileStorageService.getFilePath(filename, directory);
            Resource resource = new UrlResource(filePath.toUri());
            
            // 检查文件是否存在
            if (!resource.exists()) {
                return ResponseEntity.notFound().build();
            }
            
            // 确定文件的内容类型
            String contentType = determineContentType(filename);
            
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                    .body(resource);
        } catch (MalformedURLException ex) {
            log.error("文件下载失败", ex);
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * 获取文件扩展名
     */
    private String getFileExtension(String filename) {
        int lastIndexOf = filename.lastIndexOf(".");
        if (lastIndexOf == -1) {
            return null; // 没有扩展名
        }
        return filename.substring(lastIndexOf + 1);
    }
    
    /**
     * 根据文件名确定内容类型
     */
    private String determineContentType(String filename) {
        String extension = getFileExtension(filename).toLowerCase();
        switch (extension) {
            case "xlsx":
                return "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
            case "xls":
                return "application/vnd.ms-excel";
            case "csv":
                return "text/csv";
            case "py":
                return "text/x-python";
            default:
                return "application/octet-stream";
        }
    }
} 