package ptumall.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import java.util.Date;

@Data
@ApiModel("旧订单实体类")
public class Userorder {
    @ApiModelProperty(value = "订单id", example = "1")
    private Integer id;
    
    @ApiModelProperty(value = "商品名称", example = "苹果")
    private String goodsname;
    
    @ApiModelProperty(value = "购买数量", example = "2")
    private Integer number;
    
    @ApiModelProperty(value = "订单价格", example = "10")
    private Integer price;
    
    @ApiModelProperty(value = "下单时间", example = "2023-05-07")
    private Date time;
    
    @ApiModelProperty(value = "用户id", example = "1")
    private Integer uid;
    
    public Userorder() {
    }
    
    public Userorder(int number, int price, String goodsname, Integer uid) {
        this.number = number;
        this.price = price;
        this.goodsname = goodsname;
        this.uid = uid;
    }
}
