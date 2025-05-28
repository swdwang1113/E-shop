package ptumall.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new JWTInterceptors()).
                addPathPatterns("/**")//所有接口进行拦截
                .excludePathPatterns("/user/login","/user/register","/user/check-username")//登录、注册、检查用户名放行
                .excludePathPatterns("/captcha/get")//验证码接口放行
                .excludePathPatterns("/goods/list","/goods/*","/goods/recommend")//商品查询接口放行
                .excludePathPatterns("/category/list","/category/*")//商品分类查询接口放行
                .excludePathPatterns("/img/**")//放行图片
                .excludePathPatterns("/swagger-resources/**","/swagger-ui/**", "/v3/**", "/error");//放行swagger

    }
    
    @Value("${file.save-path}")
    String filePath;
    
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        System.out.println("配置静态资源映射路径: " + filePath);
        //其中img表示访问的前缀。"file:"是文件真实的存储路径
        registry.addResourceHandler("/img/**").addResourceLocations("file:" + filePath);
        WebMvcConfigurer.super.addResourceHandlers(registry);
    }
    
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowCredentials(true)
                .maxAge(3600)
                .allowedHeaders("*");
    }
}
