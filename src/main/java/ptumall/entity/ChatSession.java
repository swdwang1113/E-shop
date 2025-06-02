package ptumall.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import java.util.Date;

/**
 * 聊天会话实体类
 */
@Data
@ApiModel(description = "聊天会话信息")
public class ChatSession {
    @ApiModelProperty(value = "会话ID", example = "2c9ba0837b794f96017b7951c5e10000")
    private String id;               // 会话ID
    
    @ApiModelProperty(value = "客户ID", example = "1")
    private Long customerId;         // 客户ID
    
    @ApiModelProperty(value = "管理员ID", example = "2")
    private Long adminId;            // 管理员ID
    
    @ApiModelProperty(value = "创建时间")
    private Date createTime;         // 创建时间
    
    @ApiModelProperty(value = "最后更新时间")
    private Date lastUpdateTime;     // 最后更新时间
    
    @ApiModelProperty(value = "会话状态", notes = "0:进行中/1:已结束", example = "0")
    private Integer status;          // 状态（0进行中/1已结束）
    
    @ApiModelProperty(value = "会话标题", example = "商品咨询")
    private String title;            // 会话标题
} 