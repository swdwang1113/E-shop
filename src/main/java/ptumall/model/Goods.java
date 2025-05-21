package ptumall.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import java.math.BigDecimal;
import java.util.Date;

@Data
@ApiModel("商品实体类")
public class Goods {
    @ApiModelProperty(value = "商品ID", example = "1")
    private Integer id;
    
    @ApiModelProperty(value = "商品名称", example = "苹果")
    private String name;
    
    @ApiModelProperty(value = "商品价格", example = "5.00")
    private BigDecimal price;
    
    @ApiModelProperty(value = "商品描述", example = "新鲜红富士苹果")
    private String description;
    
    @ApiModelProperty(value = "商品类别ID", example = "1")
    private Integer categoryId;
    
    @ApiModelProperty(value = "库存", example = "100")
    private Integer stock;
    
    @ApiModelProperty(value = "状态", example = "1", notes = "1-上架 0-下架")
    private Byte status;
    
    @ApiModelProperty(value = "商品图片地址", example = "/img/goods/apple.jpg")
    private String imageUrl;
    
    @ApiModelProperty(value = "评分", example = "4.5", notes = "商品评分，1-5分")
    private BigDecimal rating;
    
    @ApiModelProperty(value = "销量", example = "1000", notes = "商品销量")
    private Integer salesVolume;
    
    @ApiModelProperty(value = "创建时间", hidden = true)
    private Date createTime;
    
    @ApiModelProperty(value = "更新时间", hidden = true)
    private Date updateTime;
    
    /**
     * 获取商品图片，作为imageUrl的别名
     * @return 商品图片地址
     */
    public String getImage() {
        return this.imageUrl;
    }
}
