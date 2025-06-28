package ptumall.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ptumall.service.UserService;
import ptumall.vo.Result;
import ptumall.vo.ResultCode;

import java.util.Map;

/**
 * 邮箱登录控制器
 * 处理用户通过邮箱验证码方式进行登录的相关请求
 * 包括：发送验证码和验证码登录两个主要功能
 * 属于无密码登录方案的一种实现
 */
@Slf4j 
@Api(tags = "邮箱登录接口")  
@RestController  
@RequestMapping("/api/email") 
public class EmailLoginController {

    @Autowired
    private UserService userService;
    
    /**
     * 发送邮箱验证码
     * 接收用户提供的邮箱地址，发送验证码到该邮箱
     * 用户可以使用收到的验证码进行登录
     * 
     * @param params 请求参数Map，包含email字段
     * @return 处理结果，成功返回true，失败返回错误信息
     */
    @ApiOperation("发送邮箱验证码")  // Swagger注解，描述API操作
    @PostMapping("/code/send")  // 映射POST请求到/api/email/code/send路径
    public Result<Boolean> sendEmailCode(@RequestBody Map<String, String> params) {
        // 从请求参数中获取邮箱地址
        String email = params.get("email");
        
        // 验证邮箱参数是否为空
        if (email == null || email.isEmpty()) {
            return Result.failure(ResultCode.PARAMS_IS_BLANK, "邮箱不能为空");
        }
        
        // 调用服务层发送验证码
        // 如果邮箱未注册，可能会发送失败
        boolean sent = userService.sendEmailCode(email);
        
        // 根据发送结果返回不同的响应
        if (sent) {
            log.info("邮箱验证码发送成功: {}", email);
            return Result.success(true);
        } else {
            log.warn("邮箱验证码发送失败: {}", email);
            return Result.failure(ResultCode.PARAMS_IS_INVALID, "验证码发送失败，请确认邮箱是否已注册");
        }
    }
    
    /**
     * 邮箱验证码登录
     * 接收用户提供的邮箱地址和验证码，验证后完成登录
     * 登录成功后返回用户信息和token等数据
     * 
     * @param params 请求参数Map，包含email和code字段
     * @return 处理结果，成功返回用户信息和token，失败返回错误信息
     */
    @ApiOperation("邮箱验证码登录")  // Swagger注解，描述API操作
    @PostMapping("/login")  // 映射POST请求到/api/email/login路径
    public Result<Map<String, Object>> loginByEmail(@RequestBody Map<String, String> params) {
        // 从请求参数中获取邮箱地址和验证码
        String email = params.get("email");
        String code = params.get("code");
        
        // 验证邮箱参数是否为空
        if (email == null || email.isEmpty()) {
            return Result.failure(ResultCode.PARAMS_IS_BLANK, "邮箱不能为空");
        }
        
        // 验证验证码参数是否为空
        if (code == null || code.isEmpty()) {
            return Result.failure(ResultCode.PARAMS_IS_BLANK, "验证码不能为空");
        }
        
        // 调用服务层验证邮箱和验证码，完成登录
        // loginResult包含用户信息、token等登录成功后需要的数据
        Map<String, Object> loginResult = userService.loginByEmailCode(email, code);
        
        // 根据登录结果返回不同的响应
        if (loginResult != null) {
            log.info("用户通过邮箱验证码登录成功: {}", email);
            return Result.success(loginResult);
        } else {
            log.warn("用户邮箱验证码登录失败: {}, 验证码错误或已过期", email);
            return Result.failure(ResultCode.PARAMS_IS_INVALID, "验证码错误或已过期");
        }
    }
} 