package ptumall.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import java.util.Date;

@Data
@ApiModel("商品分类实体类")
public class GoodsCategory {
    @ApiModelProperty(value = "分类ID", example = "1")
    private Integer id;
    
    @ApiModelProperty(value = "分类名称", example = "水果")
    private String name;
    
    @ApiModelProperty(value = "父分类ID", example = "0", notes = "0表示一级分类")
    private Integer parentId;
    
    @ApiModelProperty(value = "分类级别", example = "1", notes = "1-一级 2-二级 3-三级")
    private Integer level;
    
    @ApiModelProperty(value = "排序值", example = "1")
    private Integer sort;
    
    @ApiModelProperty(value = "创建时间", hidden = true)
    private Date createTime;
    
    @ApiModelProperty(value = "更新时间", hidden = true)
    private Date updateTime;
} 