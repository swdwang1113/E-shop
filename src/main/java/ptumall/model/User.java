package ptumall.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import java.util.Date;

@Data
@ApiModel("用户实体类")
public class User {
    @ApiModelProperty(value = "用户ID", required = false, example = "1")
    private Integer id;
    
    @ApiModelProperty(value = "用户名", required = true, example = "user123")
    private String username;
    
    @ApiModelProperty(value = "密码", required = true, example = "password123")
    private String password;
    
    @ApiModelProperty(value = "性别", required = false, example = "男")
    private String gender;
    
    @ApiModelProperty(value = "用户角色", notes = "0-普通用户，1-管理员", example = "0")
    private Byte role;
    
    @ApiModelProperty(value = "电话号码", required = false, example = "13800138000")
    private String phone;
    
    @ApiModelProperty(value = "邮箱", required = false, example = "user@example.com")
    private String email;
    
    @ApiModelProperty(value = "头像URL", required = false, example = "/uploads/avatars/default.jpg")
    private String avatar;
    
    @ApiModelProperty(value = "创建时间", hidden = true)
    private Date createTime;
    
    @ApiModelProperty(value = "更新时间", hidden = true)
    private Date updateTime;
}
