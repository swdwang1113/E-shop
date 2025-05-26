package ptumall.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import java.util.Date;

@Data
@ApiModel("评价点赞实体类")
public class ReviewLike {
    @ApiModelProperty(value = "点赞ID", example = "1")
    private Integer id;
    
    @ApiModelProperty(value = "用户ID", example = "1")
    private Integer userId;
    
    @ApiModelProperty(value = "评价ID", example = "1")
    private Integer reviewId;
    
    @ApiModelProperty(value = "创建时间", hidden = true)
    private Date createTime;
} 