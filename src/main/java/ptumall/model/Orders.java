package ptumall.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Data
@ApiModel("订单实体类")
public class Orders {
    @ApiModelProperty(value = "订单ID", example = "1")
    private Integer id;
    
    @ApiModelProperty(value = "用户ID", example = "1")
    private Integer userId;
    
    @ApiModelProperty(value = "订单编号", example = "202405160001")
    private String orderNo;
    
    @ApiModelProperty(value = "订单总金额", example = "99.99")
    private BigDecimal totalAmount;
    
    @ApiModelProperty(value = "订单状态", example = "0", notes = "0-待付款 1-已付款 2-已发货 3-已完成 4-已取消")
    private Byte status;
    
    @ApiModelProperty(value = "支付方式", example = "1", notes = "1-支付宝 2-微信")
    private Byte paymentType;
    
    @ApiModelProperty(value = "支付时间")
    private Date paymentTime;
    
    @ApiModelProperty(value = "发货时间")
    private Date shippingTime;
    
    @ApiModelProperty(value = "完成时间")
    private Date completionTime;
    
    @ApiModelProperty(value = "收货地址ID", example = "1")
    private Integer addressId;
    
    @ApiModelProperty(value = "创建时间", hidden = true)
    private Date createTime;
    
    @ApiModelProperty(value = "更新时间", hidden = true)
    private Date updateTime;
    
    // 非数据库字段，关联信息
    @ApiModelProperty(value = "订单商品列表", notes = "非数据库字段")
    private transient List<OrderItems> orderItems;
    
    @ApiModelProperty(value = "收货地址信息", notes = "非数据库字段")
    private transient UserAddress address;
} 