package ptumall.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web配置类
 * 用于配置Spring MVC的Web相关功能
 * 包括：拦截器配置、静态资源映射、跨域配置等
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {
    
    /**
     * 配置拦截器
     * 用于添加JWT token验证等拦截功能
     * 
     * @param registry 拦截器注册表
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new JWTInterceptors())
                .addPathPatterns("/**")  // 拦截所有请求
                .excludePathPatterns("/user/login","/user/register","/user/check-username")  // 登录、注册、检查用户名接口不需要验证
                .excludePathPatterns("/captcha/get")  // 验证码接口不需要验证
                .excludePathPatterns("/goods/list","/goods/*","/goods/recommend")  // 商品相关查询接口不需要验证
                .excludePathPatterns("/category/list","/category/*")  // 商品分类查询接口不需要验证
                .excludePathPatterns("/img/**")  // 图片资源不需要验证
                .excludePathPatterns("/swagger-resources/**","/swagger-ui/**", "/v3/**", "/error") // Swagger相关接口不需要验证
                .excludePathPatterns("/api/alipay/**")  // 支付宝相关接口不需要验证
                .excludePathPatterns("/api/email/code/send", "/api/email/login");  // 邮箱验证码登录相关接口不需要验证
    }
    
    /**
     * 文件保存路径
     * 从配置文件中读取
     */
    @Value("${file.save-path}")
    String filePath;
    
    /**
     * 配置静态资源映射
     * 用于处理图片等静态资源的访问
     * 
     * @param registry 资源处理器注册表
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        System.out.println("配置静态资源映射路径: " + filePath);
        // 配置图片访问路径映射
        // /img/** 表示访问的前缀
        // file: 表示文件真实的存储路径
        registry.addResourceHandler("/img/**").addResourceLocations("file:" + filePath);
        WebMvcConfigurer.super.addResourceHandlers(registry);
    }
    
    /**
     * 配置跨域请求处理
     * 允许前端跨域访问后端接口
     * 
     * @param registry 跨域注册表
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")  // 允许跨域的路径
                .allowedOriginPatterns("*")  // 允许所有来源
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")  // 允许的请求方法
                .allowCredentials(true)  // 允许携带认证信息（如cookie）
                .maxAge(3600)  // 预检请求的有效期，单位为秒
                .allowedHeaders("*");  // 允许所有请求头
    }
}
