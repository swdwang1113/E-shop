package ptumall.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import ptumall.service.FileService;
import ptumall.service.impl.LocalFileServiceImpl;
import ptumall.service.impl.OssFileServiceImpl;

/**
 * 文件存储配置类
 */
@Configuration
public class FileStorageConfig {

    @Value("${file.storage-type}")
    private String storageType;
    
    @Autowired
    private LocalFileServiceImpl localFileService;
    
    @Autowired
    private OssFileServiceImpl ossFileService;
    
    /**
     * 根据配置选择文件存储服务
     * 支持本地存储和阿里云OSS存储
     */
    @Bean
    @Primary
    public FileService fileService() {
        // 根据配置选择存储实现
        switch (storageType.toLowerCase()) {
            case "local":
                return localFileService;
            case "oss":
                return ossFileService;
            default:
                return localFileService;
        }
    }
} 