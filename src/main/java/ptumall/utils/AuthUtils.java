package ptumall.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ptumall.config.JWTInterceptors;
import ptumall.model.User;
import ptumall.service.UserService;

import javax.servlet.http.HttpServletRequest;

/**
 * 权限验证工具类
 * 用于处理用户权限相关的验证操作
 * 主要功能包括：管理员权限验证、用户角色判断等
 */
@Component
public class AuthUtils {

    /**
     * 用户服务接口
     * 用于获取用户信息和进行用户相关的操作
     */
    @Autowired
    private UserService userService;
    
    /**
     * 判断当前请求的用户是否具有管理员权限
     * 验证流程：
     * 1. 从请求中获取用户ID（由JWT拦截器存储）
     * 2. 根据用户ID查询用户信息
     * 3. 判断用户角色是否为管理员(role=1)
     *
     * @param request HTTP请求对象，包含用户认证信息
     * @return 如果用户是管理员返回true，否则返回false
     */
    public boolean isAdmin(HttpServletRequest request) {
        // 从请求属性中获取JWT拦截器存储的用户ID
        // JWT拦截器在验证token时会将用户ID存储在请求属性中
        Integer userId = (Integer) request.getAttribute(JWTInterceptors.USER_ID_KEY);
        if (userId == null) {
            return false;  // 如果获取不到用户ID，说明用户未登录或token无效
        }
        
        // 根据用户ID查询用户信息
        // 如果用户不存在，返回false
        User user = userService.getUserById(userId);
        if (user == null) {
            return false;
        }
        
        // 判断用户角色是否为管理员
        // role=1 表示管理员角色
        // role=0 表示普通用户角色
        return user.getRole() != null && user.getRole() == 1;
    }
} 