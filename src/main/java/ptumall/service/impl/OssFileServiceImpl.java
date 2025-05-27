package ptumall.service.impl;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.CannedAccessControlList;
import com.aliyun.oss.model.ObjectMetadata;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ptumall.service.FileService;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

/**
 * 阿里云OSS文件存储实现类
 */
@Service
public class OssFileServiceImpl implements FileService {

    @Value("${aliyun.oss.endpoint}")
    private String endpoint;

    @Value("${aliyun.oss.accessKeyId}")
    private String accessKeyId;

    @Value("${aliyun.oss.accessKeySecret}")
    private String accessKeySecret;

    @Value("${aliyun.oss.bucketName}")
    private String bucketName;
    
    @Value("${aliyun.oss.urlPrefix}")
    private String urlPrefix;

    @Override
    public String uploadImage(MultipartFile file, String directory) {
        if (file.isEmpty()) {
            System.out.println("上传的文件为空");
            return null;
        }
        
        try {
            // 获取文件名
            String fileName = file.getOriginalFilename();
            System.out.println("原始文件名: " + fileName);
            
            // 获取文件后缀
            String suffixName = fileName.substring(fileName.lastIndexOf("."));
            System.out.println("文件后缀: " + suffixName);
            
            // 生成新文件名
            String newFileName = UUID.randomUUID().toString() + suffixName;
            System.out.println("新文件名: " + newFileName);
            
            // 确定子目录
            String subDir = directory != null && !directory.isEmpty() ? directory + "/" : "";
            
            // 构建OSS中的对象名
            String objectName = "img/" + subDir + newFileName;
            System.out.println("OSS对象名: " + objectName);
            
            // 创建OSSClient实例
            OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
            
            try {
                // 设置文件元数据
                ObjectMetadata metadata = new ObjectMetadata();
                metadata.setContentType(file.getContentType());
                metadata.setContentLength(file.getSize());
                
                // 上传文件流
                InputStream inputStream = file.getInputStream();
                ossClient.putObject(bucketName, objectName, inputStream, metadata);
                
                // 返回图片访问路径
                String resultPath = urlPrefix + "/" + objectName;
                System.out.println("返回的图片URL: " + resultPath);
                return resultPath;
            } finally {
                // 关闭OSSClient
                if (ossClient != null) {
                    ossClient.shutdown();
                }
            }
        } catch (IOException e) {
            System.out.println("文件上传发生异常: " + e.getMessage());
            e.printStackTrace();
            return null;
        } catch (Exception e) {
            System.out.println("其他异常: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public boolean deleteImage(String imageUrl) {
        if (imageUrl == null || imageUrl.isEmpty()) {
            return false;
        }
        
        try {
            // 从URL中提取对象名
            String objectName = imageUrl.replace(urlPrefix + "/", "");
            
            // 创建OSSClient实例
            OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
            
            try {
                // 删除文件
                ossClient.deleteObject(bucketName, objectName);
                return true;
            } finally {
                // 关闭OSSClient
                if (ossClient != null) {
                    ossClient.shutdown();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
} 