package ptumall.controller;

import com.google.code.kaptcha.impl.DefaultKaptcha;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.imageio.ImageIO;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.awt.image.BufferedImage;
import java.io.IOException;

/**
 * 验证码控制器
 * 提供验证码生成和获取功能
 * 使用Kaptcha框架生成图片验证码
 */
@Api(tags = "验证码接口")
@RestController
@RequestMapping("/captcha")
public class CaptchaController {

    /**
     * Kaptcha验证码生成器
     * 用于生成图片验证码
     */
    @Autowired
    private DefaultKaptcha defaultKaptcha;

    /**
     * 获取验证码
     * 生成图片验证码并返回给前端
     * 同时将验证码文本存储在session中
     * 
     * @param request HTTP请求对象
     * @param response HTTP响应对象
     * @throws IOException 如果图片输出过程中发生IO异常
     */
    @ApiOperation("获取验证码")
    @GetMapping("/get")
    public void getCaptcha(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // 设置响应头，禁止浏览器缓存验证码图片
        response.setDateHeader("Expires", 0);
        response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate");
        response.addHeader("Cache-Control", "post-check=0, pre-check=0");
        response.setHeader("Pragma", "no-cache");
        // 设置响应内容类型为图片
        response.setContentType("image/jpeg");
        
        // 生成随机验证码文本
        String capText = defaultKaptcha.createText();
        
        // 将验证码文本存入session，用于后续验证
        HttpSession session = request.getSession();
        session.setAttribute("captchaCode", capText);
        
        // 根据验证码文本生成图片
        BufferedImage image = defaultKaptcha.createImage(capText);
        // 获取响应输出流
        ServletOutputStream out = response.getOutputStream();
        
        // 将图片写入响应输出流
        ImageIO.write(image, "jpg", out);
        // 刷新并关闭输出流
        out.flush();
        out.close();
    }
} 