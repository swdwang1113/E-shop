package ptumall.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ptumall.config.JWTInterceptors;
import ptumall.model.User;
import ptumall.service.FileService;
import ptumall.service.UserService;
import ptumall.utils.JWTUtils;
import ptumall.vo.LoginParam;
import ptumall.vo.Result;
import ptumall.vo.ResultCode;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

@Api(tags = "用户接口")
@RestController
@RequestMapping("/user")
public class UserController {
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private FileService fileService;
    
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
    public Result<Map<String, String>> login(@RequestBody LoginParam loginParam, HttpServletRequest request) {
        // 基本参数验证
        if (loginParam.getUsername() == null || loginParam.getUsername().trim().isEmpty() ||
            loginParam.getPassword() == null || loginParam.getPassword().trim().isEmpty() ||
            loginParam.getCaptcha() == null || loginParam.getCaptcha().trim().isEmpty()) {
            return Result.failure(ResultCode.PARAMS_IS_BLANK);
        }
        
        // 验证码校验
        HttpSession session = request.getSession();
        String captchaCode = (String) session.getAttribute("captchaCode");
        if (captchaCode == null || !captchaCode.equalsIgnoreCase(loginParam.getCaptcha())) {
            return Result.failure(ResultCode.FAILED, "验证码错误");
        }
        
        // 验证码使用后立即清除，防止重复使用
        session.removeAttribute("captchaCode");
        
        // 登录验证
        User userFromDb = userService.login(loginParam.getUsername(), loginParam.getPassword());
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
    
    @ApiOperation("更新用户信息")
    @PutMapping("/update-info")
    public Result<User> updateUserInfo(@RequestBody User user, HttpServletRequest request) {
        // 从请求属性中获取JWT拦截器存储的用户ID
        Integer userId = (Integer) request.getAttribute(JWTInterceptors.USER_ID_KEY);
        if (userId == null) {
            return Result.failure(ResultCode.UNAUTHORIZED, "请先登录");
        }
        
        // 设置用户ID，防止修改其他用户信息
        user.setId(userId);
        
        // 不允许通过此接口修改密码和角色
        user.setPassword(null);
        user.setRole(null);
        
        // 更新用户信息
        boolean updated = userService.updateUser(user);
        if (updated) {
            // 获取更新后的用户信息
            User updatedUser = userService.getUserById(userId);
            updatedUser.setPassword(null);
            return Result.success(updatedUser);
        } else {
            return Result.failure(ResultCode.FAILED, "更新用户信息失败");
        }
    }
    
    @ApiOperation("上传用户头像")
    @PostMapping("/upload-avatar")
    public Result<String> uploadAvatar(@ApiParam(value = "头像图片", required = true) 
                                        @RequestParam("file") MultipartFile file,
                                        HttpServletRequest request) {
        // 从请求属性中获取JWT拦截器存储的用户ID
        Integer userId = (Integer) request.getAttribute(JWTInterceptors.USER_ID_KEY);
        if (userId == null) {
            return Result.failure(ResultCode.UNAUTHORIZED, "请先登录");
        }
        
        // 检查文件类型
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            return Result.failure(ResultCode.PARAM_ERROR, "只能上传图片文件");
        }
        
        // 上传头像到avatars目录
        String avatarUrl = fileService.uploadImage(file, "avatars");
        if (avatarUrl == null) {
            return Result.failure(ResultCode.FAILED, "头像上传失败");
        }
        
        // 更新用户头像URL
        User user = new User();
        user.setId(userId);
        user.setAvatar(avatarUrl);
        
        boolean updated = userService.updateUser(user);
        if (updated) {
            return Result.success(avatarUrl);
        } else {
            // 如果更新失败，删除已上传的图片
            fileService.deleteImage(avatarUrl);
            return Result.failure(ResultCode.FAILED, "更新用户头像失败");
        }
    }
}
