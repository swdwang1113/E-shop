package ptumall.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel("登录参数")
public class LoginParam {
    
    @ApiModelProperty(value = "用户名", required = true)
    private String username;
    
    @ApiModelProperty(value = "密码", required = true)
    private String password;
    
    @ApiModelProperty(value = "验证码", required = true)
    private String captcha;
} 