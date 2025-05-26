package ptumall.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import java.util.Date;

@Data
@ApiModel("用户收货地址实体类")
public class UserAddress {
    @ApiModelProperty(value = "地址ID", example = "1")
    private Integer id;
    
    @ApiModelProperty(value = "用户ID", example = "1")
    private Integer userId;
    
    @ApiModelProperty(value = "收货人姓名", example = "张三")
    private String name;
    
    @ApiModelProperty(value = "手机号", example = "13800138000")
    private String phone;
    
    @ApiModelProperty(value = "省份", example = "广东省")
    private String province;
    
    @ApiModelProperty(value = "城市", example = "深圳市")
    private String city;
    
    @ApiModelProperty(value = "区/县", example = "南山区")
    private String district;
    
    @ApiModelProperty(value = "详细地址", example = "科技园路1号")
    private String address;
    
    @ApiModelProperty(value = "是否为默认地址", example = "0", notes = "0-否 1-是")
    private Byte isDefault;
    
    @ApiModelProperty(value = "创建时间", hidden = true)
    private Date createTime;
    
    @ApiModelProperty(value = "更新时间", hidden = true)
    private Date updateTime;
} 