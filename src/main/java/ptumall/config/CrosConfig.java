package ptumall.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.Collections;

/**
 * 跨域资源共享(CORS)配置类
 * 
 * CORS是一种机制，允许浏览器向跨源(不同域名、协议或端口)服务器发起请求
 * 该配置解决前后端分离架构中的跨域请求问题
 */
@Configuration
public class CrosConfig {
    /**
     * 创建CORS过滤器Bean
     * 
     * @return CorsFilter 处理跨域请求的过滤器
     */
    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration corsConfiguration = new CorsConfiguration();
        //1,允许任何来源访问（使用模式匹配，比直接使用"*"更灵活）
        corsConfiguration.setAllowedOriginPatterns(Collections.singletonList("*"));
        //2,允许任何请求头（包括自定义请求头）
        corsConfiguration.addAllowedHeader(CorsConfiguration.ALL);
        //3,允许任何HTTP方法（GET, POST, PUT, DELETE等）
        corsConfiguration.addAllowedMethod(CorsConfiguration.ALL);
        //4,允许发送身份凭证信息（如Cookie）
        //  注意：设为true时，前端使用axios等工具也需要配置withCredentials=true
        corsConfiguration.setAllowCredentials(true);

        // 创建URL基础配置源
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        // 对所有路径应用上述CORS配置
        source.registerCorsConfiguration("/**", corsConfiguration);
        // 返回配置好的CORS过滤器
        return new CorsFilter(source);
    }
}
