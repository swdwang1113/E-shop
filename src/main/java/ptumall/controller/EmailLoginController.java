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
     */
    @ApiOperation("发送邮箱验证码")
    @PostMapping("/code/send")
    public Result<Boolean> sendEmailCode(@RequestBody Map<String, String> params) {
        String email = params.get("email");
        
        if (email == null || email.isEmpty()) {
            return Result.failure(ResultCode.PARAMS_IS_BLANK, "邮箱不能为空");
        }
        
        boolean sent = userService.sendEmailCode(email);
        
        if (sent) {
            return Result.success(true);
        } else {
            return Result.failure(ResultCode.PARAMS_IS_INVALID, "验证码发送失败，请确认邮箱是否已注册");
        }
    }
    
    /**
     * 邮箱验证码登录
     */
    @ApiOperation("邮箱验证码登录")
    @PostMapping("/login")
    public Result<Map<String, Object>> loginByEmail(@RequestBody Map<String, String> params) {
        String email = params.get("email");
        String code = params.get("code");
        
        if (email == null || email.isEmpty()) {
            return Result.failure(ResultCode.PARAMS_IS_BLANK, "邮箱不能为空");
        }
        
        if (code == null || code.isEmpty()) {
            return Result.failure(ResultCode.PARAMS_IS_BLANK, "验证码不能为空");
        }
        
        Map<String, Object> loginResult = userService.loginByEmailCode(email, code);
        
        if (loginResult != null) {
            return Result.success(loginResult);
        } else {
            return Result.failure(ResultCode.PARAMS_IS_INVALID, "验证码错误或已过期");
        }
    }
} 