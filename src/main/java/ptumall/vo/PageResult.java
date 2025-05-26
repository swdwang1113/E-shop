package ptumall.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
@ApiModel("分页结果")
public class PageResult<T> {
    
    @ApiModelProperty(value = "总记录数", example = "100")
    private long total;
    
    @ApiModelProperty(value = "总页数", example = "10")
    private int pages;
    
    @ApiModelProperty(value = "当前页码", example = "1")
    private int pageNum;
    
    @ApiModelProperty(value = "每页记录数", example = "10")
    private int pageSize;
    
    @ApiModelProperty(value = "数据列表")
    private List<T> list;
    
    public PageResult() {
    }
    
    public PageResult(long total, int pages, int pageNum, int pageSize, List<T> list) {
        this.total = total;
        this.pages = pages;
        this.pageNum = pageNum;
        this.pageSize = pageSize;
        this.list = list;
    }
} 