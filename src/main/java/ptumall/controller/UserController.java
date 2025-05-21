package ptumall.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ptumall.config.JWTInterceptors;
import ptumall.model.User;
import ptumall.service.UserService;
import ptumall.utils.JWTUtils;
import ptumall.vo.Result;
import ptumall.vo.ResultCode;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@Api(tags = "用户接口")
@RestController
@RequestMapping("/user")
public class UserController {
    
    @Autowired
    private UserService userService;
    
    @ApiOperation("用户注册")
    @PostMapping("/register")
    public Result<User> register(@RequestBody User user) {
        // 基本参数验证
        if (user.getUsername() == null || user.getUsername().trim().isEmpty() ||
            user.getPassword() == null || user.getPassword().trim().isEmpty()) {
            return Result.failure(ResultCode.PARAMS_IS_BLANK);
        }
        
        // 检查用户名是否已存在
        if (userService.checkUsernameExists(user.getUsername())) {
            return Result.failure(ResultCode.USER_IS_EXITES);
        }
        
        // 注册用户
        User registeredUser = userService.register(user);
        if (registeredUser != null) {
            // 注册成功，返回用户信息（不包含密码）
            registeredUser.setPassword(null);
            return Result.success(registeredUser);
        } else {
            return Result.failure(ResultCode.FAILED, "注册失败，请稍后重试");
        }
    }
    
    @ApiOperation("用户登录")
    @PostMapping("/login")
    public Result<Map<String, String>> login(@RequestBody User user) {
        // 基本参数验证
        if (user.getUsername() == null || user.getUsername().trim().isEmpty() ||
            user.getPassword() == null || user.getPassword().trim().isEmpty()) {
            return Result.failure(ResultCode.PARAMS_IS_BLANK);
        }
        
        // 登录验证
        User userFromDb = userService.login(user.getUsername(), user.getPassword());
        if (userFromDb == null) {
            return Result.failure(ResultCode.UNAUTHORIZED, "用户名或密码错误");
        }
        
        // 生成JWT令牌
        String token = JWTUtils.getToken(userFromDb.getId(), userFromDb.getUsername());
        
        // 组装返回数据
        Map<String, String> userInfo = new HashMap<>();
        userInfo.put("userId", userFromDb.getId().toString());
        userInfo.put("username", userFromDb.getUsername());
        userInfo.put("token", token);
        
        return Result.success(userInfo);
    }
    
    @ApiOperation("检查用户名是否可用")
    @GetMapping("/check-username")
    public Result<Boolean> checkUsername(@ApiParam(value = "用户名", required = true) 
                                          @RequestParam String username) {
        boolean exists = userService.checkUsernameExists(username);
        return Result.success(!exists);
    }
    
    @ApiOperation("获取用户信息")
    @GetMapping("/info")
    public Result<User> getUserInfo(HttpServletRequest request) {
        // 从请求属性中获取JWT拦截器存储的用户ID
        Integer userId = (Integer) request.getAttribute(JWTInterceptors.USER_ID_KEY);
        if (userId == null) {
            return Result.failure(ResultCode.UNAUTHORIZED, "请先登录");
        }
        
        User user = userService.getUserById(userId);
        if (user != null) {
            // 返回用户信息，不包含密码
            user.setPassword(null);
            return Result.success(user);
        } else {
            return Result.failure(ResultCode.NOT_FOUND, "用户不存在");
        }
    }
}
