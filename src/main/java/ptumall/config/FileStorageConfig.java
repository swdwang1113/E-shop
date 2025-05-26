package ptumall.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import ptumall.service.FileService;
import ptumall.service.impl.LocalFileServiceImpl;

/**
 * 文件存储配置类
 */
@Configuration
public class FileStorageConfig {

    @Value("${file.storage-type}")
    private String storageType;
    
    @Autowired
    private LocalFileServiceImpl localFileService;
    
    /**
     * 根据配置选择文件存储服务
     * 目前只有本地存储，未来可扩展为OSS等云存储
     */
    @Bean
    @Primary
    public FileService fileService() {
        // 根据配置选择存储实现
        switch (storageType.toLowerCase()) {
            case "local":
            default:
                return localFileService;
            // 未来可以添加OSS等其他存储方式
            // case "oss":
            //     return ossFileService;
        }
    }
} 