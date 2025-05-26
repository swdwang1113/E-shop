package ptumall.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ptumall.config.JWTInterceptors;
import ptumall.model.User;
import ptumall.service.UserService;

import javax.servlet.http.HttpServletRequest;

/**
 * 权限验证工具类
 */
@Component
public class AuthUtils {

    @Autowired
    private UserService userService;
    
    /**
     * 判断用户是否具有管理员权限
     *
     * @param request HTTP请求
     * @return 如果用户是管理员，返回true；否则返回false
     */
    public boolean isAdmin(HttpServletRequest request) {
        // 从请求属性中获取JWT拦截器存储的用户ID
        Integer userId = (Integer) request.getAttribute(JWTInterceptors.USER_ID_KEY);
        if (userId == null) {
            return false;
        }
        
        // 获取用户信息
        User user = userService.getUserById(userId);
        if (user == null) {
            return false;
        }
        
        // 判断用户角色是否为管理员(role=1)
        return user.getRole() != null && user.getRole() == 1;
    }
} 