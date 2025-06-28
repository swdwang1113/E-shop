package ptumall.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import ptumall.service.EmailService;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

/**
 * 邮件服务实现类
 * 负责处理系统中所有邮件发送相关的功能
 * 目前主要用于发送验证码邮件，支持HTML格式内容
 */
@Slf4j  
@Service  
public class EmailServiceImpl implements EmailService {

    /**
     * Spring邮件发送器
     * 由Spring自动注入，负责底层的邮件发送功能
     * 配置在application.properties或application.yml中
     */
    @Autowired
    private JavaMailSender mailSender;
    
    /**
     * 发件人邮箱地址
     * 从配置文件中读取，通常是系统的官方邮箱地址
     */
    @Value("${spring.mail.username}")
    private String from;
    
    /**
     * 发送验证码邮件
     * 生成一封包含验证码的HTML格式邮件并发送
     * 
     * @param to 收件人邮箱地址
     * @param code 验证码
     * @return 发送结果，true表示发送成功，false表示发送失败
     */
    @Override
    public boolean sendVerificationCode(String to, String code) {
        try {
            // 创建邮件消息对象
            MimeMessage message = mailSender.createMimeMessage();
            // 创建邮件助手，设置第二个参数为true表示支持多部分消息（如附件）
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            
            // 设置邮件基本属性
            helper.setFrom(from);  // 设置发件人
            helper.setTo(to);      // 设置收件人
            helper.setSubject("登录验证码");  // 设置邮件主题
            
            // 构建HTML格式的邮件内容
            // 使用CSS样式美化邮件，提高用户体验
            String content = "<div style='padding: 20px; background-color: #f7f7f7;'>"
                    + "<h2 style='color: #333;'>您的验证码</h2>"
                    + "<p style='font-size: 16px; color: #666;'>您好，您的登录验证码为：</p>"
                    + "<div style='background-color: #fff; padding: 10px; border-radius: 5px; margin: 20px 0;'>"
                    + "<h1 style='color: #007bff; text-align: center; letter-spacing: 5px;'>" + code + "</h1>"
                    + "</div>"
                    + "<p style='font-size: 14px; color: #999;'>验证码有效期为5分钟，请勿泄露给他人。</p>"
                    + "</div>";
            
            // 设置邮件内容，第二个参数为true表示内容为HTML格式
            helper.setText(content, true);
            
            // 发送邮件
            mailSender.send(message);
            // 记录发送成功日志
            log.info("发送验证码邮件成功：{}", to);
            return true;
        } catch (MessagingException e) {
            // 捕获并记录邮件发送异常
            log.error("发送验证码邮件失败：{}", e.getMessage(), e);
            return false;
        }
    }
} 