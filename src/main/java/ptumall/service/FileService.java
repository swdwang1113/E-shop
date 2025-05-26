package ptumall.service;

import org.springframework.web.multipart.MultipartFile;

/**
 * 文件服务接口，处理图片上传存储
 */
public interface FileService {
    
    /**
     * 上传图片文件
     * @param file 上传的图片文件
     * @param directory 子目录名，如"goods"、"avatar"等
     * @return 返回可访问的图片URL
     */
    String uploadImage(MultipartFile file, String directory);
    
    /**
     * 删除图片文件
     * @param imageUrl 图片的URL
     * @return 删除是否成功
     */
    boolean deleteImage(String imageUrl);
} 