package ptumall.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 聊天会话传输对象
 */
@Data
@ApiModel(description = "聊天会话传输对象")
public class ChatSessionDTO {
    @ApiModelProperty(value = "用户ID", example = "1", required = true)
    private Long customerId;
    
    @ApiModelProperty(value = "会话标题", example = "商品咨询", required = true)
    private String title;
} 