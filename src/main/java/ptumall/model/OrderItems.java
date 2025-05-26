package ptumall.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import java.math.BigDecimal;
import java.util.Date;

@Data
@ApiModel("订单商品实体类")
public class OrderItems {
    @ApiModelProperty(value = "ID", example = "1")
    private Integer id;
    
    @ApiModelProperty(value = "订单ID", example = "1")
    private Integer orderId;
    
    @ApiModelProperty(value = "商品ID", example = "1")
    private Integer goodsId;
    
    @ApiModelProperty(value = "商品名称", example = "苹果")
    private String goodsName;
    
    @ApiModelProperty(value = "商品图片", example = "/img/goods/apple.jpg")
    private String goodsImage;
    
    @ApiModelProperty(value = "商品价格", example = "5.00")
    private BigDecimal goodsPrice;
    
    @ApiModelProperty(value = "购买数量", example = "2")
    private Integer quantity;
    
    @ApiModelProperty(value = "总价", example = "10.00")
    private BigDecimal totalPrice;
    
    @ApiModelProperty(value = "创建时间", hidden = true)
    private Date createTime;
} 