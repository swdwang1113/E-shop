package ptumall.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.oas.annotations.EnableOpenApi;
import springfox.documentation.service.*;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.contexts.SecurityContext;
import springfox.documentation.spring.web.plugins.Docket;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

/**
 * Swagger配置类
 * 用于配置API文档的生成规则和展示方式
 * 通过@EnableOpenApi注解启用Swagger功能
 */
@Configuration
@EnableOpenApi
public class SwaggerConfig {
    /**
     * 是否启用Swagger，从配置文件中读取
     */
    @Value("${swagger.enabled}")
    Boolean swaggerEnabled;

    /**
     * 配置Swagger的Docket实例
     * 用于设置API文档的基本信息、扫描范围、安全配置等
     * 
     * @return 配置好的Docket实例
     */
    @Bean
    public Docket docket() {
        return new Docket(DocumentationType.OAS_30)
                .apiInfo(apiInfo())
                // 是否开启swagger，从配置文件中读取
                .enable(swaggerEnabled)
                .select()
                // 指定要扫描的包路径，这里扫描controller包下的所有接口
                .apis(RequestHandlerSelectors.basePackage("ptumall.controller"))
                // 指定路径处理，PathSelectors.any()表示扫描所有路径
                .paths(PathSelectors.any())
                .build()
                // 配置安全认证方案
                .securitySchemes(Collections.singletonList(securityScheme()))
                // 配置安全上下文
                .securityContexts(Arrays.asList(tokenContext()));
    }

    /**
     * 配置API文档的基本信息
     * 包括标题、描述、版本、作者信息等
     * 
     * @return ApiInfo实例
     */
    private ApiInfo apiInfo() {
        /*作者信息*/
        Contact contact = new Contact("王建均", "https://ptu.com", "1476145366@qq.com");
        return new ApiInfo(
                "ptu mall",                    // 标题
                "ptu mall 测试接口文档",        // 描述
                "v1.0",                        // 版本
                "https://ptu.com",             // 服务条款URL
                contact,                       // 联系人信息
                "Apache 2.0",                  // 许可证
                "http://www.apache.org/licenses/LICENSE-2.0",  // 许可证URL
                new ArrayList()                // 扩展信息
        );
    }

    /**
     * 配置安全认证方案
     * 使用token作为认证方式，在header中传递
     * 
     * @return SecurityScheme实例
     */
    @Bean
    SecurityScheme securityScheme() {
        return new ApiKey("token", "token", "header");
    }

    /**
     * 配置安全上下文
     * 设置需要token认证的接口范围
     * 
     * @return SecurityContext实例
     */
    private SecurityContext tokenContext() {
        return SecurityContext.builder()
                .securityReferences(Arrays.asList(SecurityReference.builder()
                        .scopes(new AuthorizationScope[0])
                        .reference("token")
                        .build()))
                // 对所有接口路径进行token验证
                .operationSelector(o -> o.requestMappingPattern().matches("/.*"))
                .build();
    }
}

