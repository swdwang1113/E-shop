package ptumall.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import java.util.Date;

@Data
@ApiModel("用户收藏实体类")
public class UserFavorite {
    @ApiModelProperty(value = "收藏ID", example = "1")
    private Integer id;
    
    @ApiModelProperty(value = "用户ID", example = "1")
    private Integer userId;
    
    @ApiModelProperty(value = "商品ID", example = "1")
    private Integer goodsId;
    
    @ApiModelProperty(value = "创建时间", hidden = true)
    private Date createTime;
    
    // 非数据库字段
    @ApiModelProperty(value = "商品信息", notes = "非数据库字段")
    private transient Goods goods;
} 