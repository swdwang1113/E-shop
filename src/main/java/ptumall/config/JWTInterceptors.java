package ptumall.config;

import com.auth0.jwt.exceptions.AlgorithmMismatchException;
import com.auth0.jwt.exceptions.SignatureVerificationException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import ptumall.utils.JWTUtils;
import ptumall.vo.Result;
import ptumall.vo.ResultCode;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT拦截器
 * 用于拦截HTTP请求，验证JWT令牌的有效性，并从令牌中提取用户信息
 * 将在WebConfig中注册，用于保护需要身份验证的API端点
 */
@Component
public class JWTInterceptors implements HandlerInterceptor {
    // 用于存储在request属性中的用户ID的key
    public static final String USER_ID_KEY = "currentUserId";
    // 用于存储在request属性中的用户名的key
    public static final String USERNAME_KEY = "currentUsername";
    
    /**
     * 在请求处理之前进行调用
     * 验证请求头中的JWT令牌，提取用户信息并存储在请求属性中
     * 
     * @param request 当前HTTP请求
     * @param response HTTP响应
     * @param handler 选择要执行的处理器对象
     * @return 如果令牌有效返回true继续处理，否则返回false终止请求
     * @throws Exception 处理过程中可能抛出的异常
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        Map<String,Object> map = new HashMap<>();
        String message="";
        // 获取请求头中令牌
        String token = request.getHeader("token");

        try {
            // 验证令牌，如果无效会抛出异常
            DecodedJWT jwt = JWTUtils.verify(token);
            
            // 从JWT中获取用户信息
            String userId = null;
            // 先尝试从userId字段获取（兼容不同的JWT格式）
            userId = jwt.getClaim("userId").asString();
            // 如果userId为空，则尝试从id字段获取
            if (userId == null || userId.isEmpty()) {
                userId = jwt.getClaim("id").asString();
            }
            
            // 从JWT中获取用户名
            String username = jwt.getClaim("name").asString();
            
            // 将用户信息存储到请求属性中，以便在Controller中获取
            request.setAttribute(USER_ID_KEY, Integer.valueOf(userId));
            request.setAttribute(USERNAME_KEY, username);
            
            return true;  // 令牌验证成功，放行请求

        } catch (SignatureVerificationException e) {
            // JWT签名验证失败，可能是令牌被篡改
//            e.printStackTrace();
            message="无效签名！";
        }catch (TokenExpiredException e){
            // JWT令牌已过期，需要重新登录获取新令牌
//            e.printStackTrace();
            message="token过期";
        }catch (AlgorithmMismatchException e){
            // JWT使用的算法与验证时使用的算法不匹配
//            e.printStackTrace();
            message="算法不一致";
        }catch (Exception e){
            // 其他异常，如令牌为空、格式错误等
//            e.printStackTrace();
            message="token 为空或无效！";
        }
        // 将错误结果以JSON格式响应到前端
        // 使用Jackson库将Result对象转换为JSON字符串
        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(Result.failure(ResultCode.UNAUTHORIZED,message));
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().println(json);
        return false;  // 令牌验证失败，拒绝请求
    }
}

