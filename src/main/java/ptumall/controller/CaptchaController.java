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

@Api(tags = "验证码接口")
@RestController
@RequestMapping("/captcha")
public class CaptchaController {

    @Autowired
    private DefaultKaptcha defaultKaptcha;

    @ApiOperation("获取验证码")
    @GetMapping("/get")
    public void getCaptcha(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // 定义response输出类型为image/jpeg
        response.setDateHeader("Expires", 0);
        response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate");
        response.addHeader("Cache-Control", "post-check=0, pre-check=0");
        response.setHeader("Pragma", "no-cache");
        response.setContentType("image/jpeg");
        
        // 生成验证码文字
        String capText = defaultKaptcha.createText();
        
        // 将验证码存入session
        HttpSession session = request.getSession();
        session.setAttribute("captchaCode", capText);
        
        // 使用生成的验证码字符串返回一个BufferedImage对象并转为byte写入到byte数组中
        BufferedImage image = defaultKaptcha.createImage(capText);
        ServletOutputStream out = response.getOutputStream();
        
        // 输出图片流
        ImageIO.write(image, "jpg", out);
        out.flush();
        out.close();
    }
} 