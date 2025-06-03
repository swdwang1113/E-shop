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
 */
@Slf4j
@Service
public class EmailServiceImpl implements EmailService {

    @Autowired
    private JavaMailSender mailSender;
    
    @Value("${spring.mail.username}")
    private String from;
    
    @Override
    public boolean sendVerificationCode(String to, String code) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            
            helper.setFrom(from);
            helper.setTo(to);
            helper.setSubject("登录验证码");
            
            // 邮件内容
            String content = "<div style='padding: 20px; background-color: #f7f7f7;'>"
                    + "<h2 style='color: #333;'>您的验证码</h2>"
                    + "<p style='font-size: 16px; color: #666;'>您好，您的登录验证码为：</p>"
                    + "<div style='background-color: #fff; padding: 10px; border-radius: 5px; margin: 20px 0;'>"
                    + "<h1 style='color: #007bff; text-align: center; letter-spacing: 5px;'>" + code + "</h1>"
                    + "</div>"
                    + "<p style='font-size: 14px; color: #999;'>验证码有效期为5分钟，请勿泄露给他人。</p>"
                    + "</div>";
            
            helper.setText(content, true);
            
            mailSender.send(message);
            log.info("发送验证码邮件成功：{}", to);
            return true;
        } catch (MessagingException e) {
            log.error("发送验证码邮件失败：{}", e.getMessage(), e);
            return false;
        }
    }
} 