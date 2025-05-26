package ptumall.service.impl;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ptumall.service.FileService;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

/**
 * 本地文件存储实现类
 */
@Service
public class LocalFileServiceImpl implements FileService {

    @Value("${file.save-path}")
    private String saveFilePath;
    
    @Value("${image.prefix-url}")
    private String imageUrl;
    
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
            
            // 创建目标目录
            String dirPath = saveFilePath + subDir;
            System.out.println("目标目录路径: " + dirPath);
            
            File destDir = new File(dirPath);
            if (!destDir.exists()) {
                boolean created = destDir.mkdirs();
                System.out.println("创建目录结果: " + created);
            }
            
            // 检查目录是否存在及权限
            if (!destDir.exists() || !destDir.canWrite()) {
                System.out.println("目录不存在或没有写权限: " + dirPath);
                return null;
            }
            
            // 创建目标文件
            File dest = new File(dirPath + newFileName);
            System.out.println("目标文件路径: " + dest.getAbsolutePath());
            
            // 保存文件
            file.transferTo(dest);
            
            if (!dest.exists()) {
                System.out.println("文件保存失败，目标文件不存在");
                return null;
            }
            
            // 返回图片访问路径
            String resultPath = imageUrl + "/img/" + subDir + newFileName;
            System.out.println("返回的图片URL: " + resultPath);
            return resultPath;
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
            // 从URL中提取文件路径
            String imgPath = imageUrl.replace(this.imageUrl + "/img/", "");
            File file = new File(saveFilePath + imgPath);
            if (file.exists()) {
                return file.delete();
            }
            return true; // 文件不存在，视为删除成功
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
} 