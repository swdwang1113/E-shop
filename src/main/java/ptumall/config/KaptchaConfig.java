package ptumall.config;

import com.google.code.kaptcha.impl.DefaultKaptcha;
import com.google.code.kaptcha.util.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Properties;

/**
 * 验证码配置类
 * 该类用于配置图形验证码的生成参数，包括样式、大小、字体等属性
 * 使用Google Kaptcha库实现验证码功能
 */
@Configuration
public class KaptchaConfig {

    /**
     * 创建并配置DefaultKaptcha实例
     * 该Bean将被Spring管理并用于生成验证码图片
     * 
     * @return 配置好的DefaultKaptcha实例
     */
    @Bean
    public DefaultKaptcha getDefaultKaptcha() {
        DefaultKaptcha defaultKaptcha = new DefaultKaptcha();
        Properties properties = new Properties();
        // 图片边框：是否有边框
        properties.setProperty("kaptcha.border", "yes");
        // 边框颜色：RGB值，绿色系
        properties.setProperty("kaptcha.border.color", "105,179,90");
        // 验证码文本颜色：蓝色
        properties.setProperty("kaptcha.textproducer.font.color", "blue");
        // 图片宽度：像素值
        properties.setProperty("kaptcha.image.width", "110");
        // 图片高度：像素值
        properties.setProperty("kaptcha.image.height", "40");
        // 文本字体大小：像素值
        properties.setProperty("kaptcha.textproducer.font.size", "30");
        // 验证码在Session中的key名称
        properties.setProperty("kaptcha.session.key", "code");
        // 验证码文本长度：生成几个字符
        properties.setProperty("kaptcha.textproducer.char.length", "4");
        // 验证码文本字体样式：可选多种字体
        properties.setProperty("kaptcha.textproducer.font.names", "宋体,楷体,微软雅黑");
        
        // 创建Kaptcha配置对象并应用到DefaultKaptcha实例
        Config config = new Config(properties);
        defaultKaptcha.setConfig(config);
        
        return defaultKaptcha;
    }
} 