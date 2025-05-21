package ptumall.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import java.util.Date;
import java.math.BigDecimal;

@Data
@ApiModel("购物车实体类")
public class Cart {
    @ApiModelProperty(value = "购物车ID", example = "1")
    private Integer id;
    
    @ApiModelProperty(value = "用户ID", example = "1")
    private Integer userId;
    
    @ApiModelProperty(value = "商品ID", example = "1")
    private Integer goodsId;
    
    @ApiModelProperty(value = "商品数量", example = "2")
    private Integer quantity;
    
    @ApiModelProperty(value = "创建时间", hidden = true)
    private Date createTime;
    
    @ApiModelProperty(value = "更新时间", hidden = true)
    private Date updateTime;
    
    // 非数据库字段，用于显示
    @ApiModelProperty(value = "商品名称", notes = "非数据库字段")
    private transient String goodsName;
    
    @ApiModelProperty(value = "商品价格", notes = "非数据库字段")
    private transient BigDecimal goodsPrice;
    
    @ApiModelProperty(value = "商品图片", notes = "非数据库字段")
    private transient String goodsImage;
    
    @ApiModelProperty(value = "商品总价", notes = "非数据库字段，单价×数量")
    private transient BigDecimal totalPrice;
}
