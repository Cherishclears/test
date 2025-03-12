package com.example.demo.service.impl;

import com.example.demo.config.FileStorageProperties;
import com.example.demo.service.FileStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class FileStorageServiceImpl implements FileStorageService {

    private final Path fileStorageLocation;

    @Autowired
    public FileStorageServiceImpl(FileStorageProperties fileStorageProperties) {
        // 添加空值检查，如果uploadDir为空，则使用默认值
        String uploadDir = fileStorageProperties.getUploadDir();
        if (uploadDir == null || uploadDir.trim().isEmpty()) {
            uploadDir = "./uploads/images";
        }
        
        this.fileStorageLocation = Paths.get(uploadDir)
                .toAbsolutePath().normalize();

        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (Exception ex) {
            throw new RuntimeException("无法创建文件上传目录: " + this.fileStorageLocation, ex);
        }
    }

    @Override
    public String storeFile(MultipartFile file) {
        // 获取原始文件名
        String originalFileName = StringUtils.cleanPath(file.getOriginalFilename());
        
        // 检查文件名是否包含非法字符
        if (originalFileName.contains("..")) {
            throw new RuntimeException("文件名包含非法路径序列 " + originalFileName);
        }
        
        // 生成新的文件名，避免文件名冲突
        String fileExtension = "";
        if (originalFileName.contains(".")) {
            fileExtension = originalFileName.substring(originalFileName.lastIndexOf("."));
        }
        String newFileName = UUID.randomUUID().toString() + fileExtension;
        
        try {
            // 将文件复制到目标位置
            Path targetLocation = this.fileStorageLocation.resolve(newFileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
            
            // 返回可访问的URL路径
            return "/api/files/" + newFileName;
        } catch (IOException ex) {
            throw new RuntimeException("无法存储文件 " + originalFileName, ex);
        }
    }
} 