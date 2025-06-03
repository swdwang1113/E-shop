package ptumall.config;

import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

/**
 * 支付宝配置
 */
@Data
@Component
@Configuration
@ConfigurationProperties(prefix = "alipay")
public class AlipayConfig {
    // 应用ID,您的APPID，收款账号既是您的APPID对应支付宝账号
    private String appId;
    
    // 商户私钥，您的PKCS8格式RSA2私钥
    private String privateKey;
    
    // 支付宝公钥
    private String publicKey;
    
    // 服务器异步通知页面路径  需http://格式的完整路径，不能加?id=123这类自定义参数
    private String notifyUrl;
    
    // 页面跳转同步通知页面路径 需http://格式的完整路径，不能加?id=123这类自定义参数
    private String returnUrl;
    
    // 签名方式
    private String signType = "RSA2";
    
    // 字符编码格式
    private String charset = "utf-8";
    
    // 支付宝网关，沙箱环境时使用沙箱的网关
    private String gatewayUrl = "https://openapi.alipaydev.com/gateway.do";
    
    // 支付宝网关，正式环境时使用正式的网关
    // private String gatewayUrl = "https://openapi.alipay.com/gateway.do";
    
    // 支付宝格式
    private String format = "json";
    
    @Bean
    public AlipayClient alipayClient() {
        return new DefaultAlipayClient(gatewayUrl, appId, privateKey, format, charset, publicKey, signType);
    }
} 