package ptumall.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
@ApiModel("创建订单参数")
public class CreateOrderParam {
    
    @ApiModelProperty(value = "购物车商品ID列表，从购物车创建订单时使用", example = "[1, 2, 3]")
    private List<Integer> cartItemIds;
    
    @ApiModelProperty(value = "直接购买的商品ID，直接购买时使用", example = "1")
    private Integer goodsId;
    
    @ApiModelProperty(value = "直接购买的商品数量，直接购买时使用", example = "1")
    private Integer quantity;
    
    @ApiModelProperty(value = "收货地址ID", example = "1", required = true)
    private Integer addressId;
} 