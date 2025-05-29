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
 * 提供文件上传和删除功能
 * 使用阿里云OSS作为文件存储服务
 */
@Service
public class OssFileServiceImpl implements FileService {

    /**
     * OSS服务的地域节点
     * 例如：oss-cn-hangzhou.aliyuncs.com
     */
    @Value("${aliyun.oss.endpoint}")
    private String endpoint;

    /**
     * 阿里云账号AccessKey ID
     * 用于身份验证
     */
    @Value("${aliyun.oss.accessKeyId}")
    private String accessKeyId;

    /**
     * 阿里云账号AccessKey Secret
     * 用于身份验证
     */
    @Value("${aliyun.oss.accessKeySecret}")
    private String accessKeySecret;

    /**
     * OSS存储空间名称
     * 用于存储文件的容器
     */
    @Value("${aliyun.oss.bucketName}")
    private String bucketName;
    
    /**
     * OSS访问域名前缀
     * 用于构建文件的访问URL
     */
    @Value("${aliyun.oss.urlPrefix}")
    private String urlPrefix;

    /**
     * 上传图片到OSS
     * 支持指定子目录，自动生成唯一文件名
     * 
     * @param file 要上传的图片文件
     * @param directory 可选的子目录名称
     * @return 上传成功返回图片的访问URL，失败返回null
     */
    @Override
    public String uploadImage(MultipartFile file, String directory) {
        // 验证文件是否为空
        if (file.isEmpty()) {
            System.out.println("上传的文件为空");
            return null;
        }
        
        try {
            // 获取原始文件名
            String fileName = file.getOriginalFilename();
            System.out.println("原始文件名: " + fileName);
            
            // 提取文件后缀名
            String suffixName = fileName.substring(fileName.lastIndexOf("."));
            System.out.println("文件后缀: " + suffixName);
            
            // 使用UUID生成新的文件名，避免文件名冲突
            String newFileName = UUID.randomUUID().toString() + suffixName;
            System.out.println("新文件名: " + newFileName);
            
            // 处理子目录，如果directory为空则使用空字符串
            String subDir = directory != null && !directory.isEmpty() ? directory + "/" : "";
            
            // 构建OSS中的完整对象路径：img/子目录/文件名
            String objectName = "img/" + subDir + newFileName;
            System.out.println("OSS对象名: " + objectName);
            
            // 创建OSS客户端实例
            OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
            
            try {
                // 设置文件的元数据
                ObjectMetadata metadata = new ObjectMetadata();
                metadata.setContentType(file.getContentType());  // 设置文件类型
                metadata.setContentLength(file.getSize());      // 设置文件大小
                
                // 获取文件输入流并上传到OSS
                InputStream inputStream = file.getInputStream();
                ossClient.putObject(bucketName, objectName, inputStream, metadata);
                
                // 构建并返回文件的访问URL
                String resultPath = urlPrefix + "/" + objectName;
                System.out.println("返回的图片URL: " + resultPath);
                return resultPath;
            } finally {
                // 确保关闭OSS客户端，释放资源
                if (ossClient != null) {
                    ossClient.shutdown();
                }
            }
        } catch (IOException e) {
            // 处理文件IO异常
            System.out.println("文件上传发生异常: " + e.getMessage());
            e.printStackTrace();
            return null;
        } catch (Exception e) {
            // 处理其他异常
            System.out.println("其他异常: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 从OSS删除图片
     * 根据图片URL删除对应的OSS对象
     * 
     * @param imageUrl 要删除的图片URL
     * @return 删除成功返回true，失败返回false
     */
    @Override
    public boolean deleteImage(String imageUrl) {
        // 验证URL是否为空
        if (imageUrl == null || imageUrl.isEmpty()) {
            return false;
        }
        
        try {
            // 从完整URL中提取OSS对象名
            // 例如：从 http://example.com/img/test.jpg 提取 img/test.jpg
            String objectName = imageUrl.replace(urlPrefix + "/", "");
            
            // 创建OSS客户端实例
            OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
            
            try {
                // 执行删除操作
                ossClient.deleteObject(bucketName, objectName);
                return true;
            } finally {
                // 确保关闭OSS客户端，释放资源
                if (ossClient != null) {
                    ossClient.shutdown();
                }
            }
        } catch (Exception e) {
            // 处理删除过程中的异常
            e.printStackTrace();
            return false;
        }
    }
} 