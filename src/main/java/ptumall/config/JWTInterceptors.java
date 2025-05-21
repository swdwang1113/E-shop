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

@Component
public class JWTInterceptors implements HandlerInterceptor {
    // 用于存储在request属性中的用户ID的key
    public static final String USER_ID_KEY = "currentUserId";
    // 用于存储在request属性中的用户名的key
    public static final String USERNAME_KEY = "currentUsername";
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        Map<String,Object> map = new HashMap<>();
        String message="";
        // 获取请求头中令牌
        String token = request.getHeader("token");

        try {
            // 验证令牌
            DecodedJWT jwt = JWTUtils.verify(token);
            
            // 从JWT中获取用户信息
            String userId = null;
            // 先尝试从userId字段获取
            userId = jwt.getClaim("userId").asString();
            // 如果userId为空，则尝试从id字段获取
            if (userId == null || userId.isEmpty()) {
                userId = jwt.getClaim("id").asString();
            }
            
            String username = jwt.getClaim("name").asString();
            
            // 将用户信息存储到请求属性中，以便在Controller中获取
            request.setAttribute(USER_ID_KEY, Integer.valueOf(userId));
            request.setAttribute(USERNAME_KEY, username);
            
            return true;  // 放行请求

        } catch (SignatureVerificationException e) {
//            e.printStackTrace();
            message="无效签名！";
        }catch (TokenExpiredException e){
//            e.printStackTrace();
            message="token过期";
        }catch (AlgorithmMismatchException e){
//            e.printStackTrace();
            message="算法不一致";
        }catch (Exception e){
//            e.printStackTrace();
            message="token 为空或无效！";
        }
        // 将HttpResult以json的形式响应到前台  HttpResult --> json  (jackson)
        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(Result.failure(ResultCode.UNAUTHORIZED,message));
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().println(json);
        return false;
    }
}

