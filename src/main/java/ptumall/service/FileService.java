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
    
    /**
     * 上传文件（兼容原FileUploadUtil.uploadFile方法）
     * @param file 上传的文件
     * @param relativePath 相对路径，如"/img/refund/xxx.jpg"
     * @return 返回可访问的文件URL
     */
    default String uploadFile(MultipartFile file, String relativePath) {
        // 从relativePath中提取目录和文件名
        // 例如：从"/img/refund/xxx.jpg"提取"refund"作为目录
        String directory = "";
        if (relativePath.startsWith("/img/")) {
            int secondSlash = relativePath.indexOf("/", 5);
            if (secondSlash > 0) {
                directory = relativePath.substring(5, secondSlash);
            }
        }
        return uploadImage(file, directory);
    }
} 