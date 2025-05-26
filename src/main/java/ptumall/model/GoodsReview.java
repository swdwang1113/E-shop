package ptumall.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import java.util.Date;

@Data
@ApiModel("商品评价实体类")
public class GoodsReview {
    @ApiModelProperty(value = "评价ID", example = "1")
    private Integer id;
    
    @ApiModelProperty(value = "用户ID", example = "1")
    private Integer userId;
    
    @ApiModelProperty(value = "商品ID", example = "1")
    private Integer goodsId;
    
    @ApiModelProperty(value = "订单ID", example = "1")
    private Integer orderId;
    
    @ApiModelProperty(value = "评分", example = "5", notes = "1-5分")
    private Byte rating;
    
    @ApiModelProperty(value = "评价内容", example = "商品很好，质量不错！")
    private String content;
    
    @ApiModelProperty(value = "评价图片", example = "/img/review/1.jpg,/img/review/2.jpg", notes = "多个图片URL用逗号分隔")
    private String images;
    
    @ApiModelProperty(value = "点赞数", example = "10")
    private Integer likeCount;
    
    @ApiModelProperty(value = "创建时间", hidden = true)
    private Date createTime;
    
    // 非数据库字段
    @ApiModelProperty(value = "用户名", notes = "非数据库字段")
    private transient String username;
    
    @ApiModelProperty(value = "商品名称", notes = "非数据库字段")
    private transient String goodsName;
} 