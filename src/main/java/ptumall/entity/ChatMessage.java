package ptumall.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import java.util.Date;

/**
 * 聊天消息实体类
 */
@Data
@ApiModel(description = "聊天消息信息")
public class ChatMessage {
    @ApiModelProperty(value = "消息ID", example = "1")
    private Long id;
    
    @ApiModelProperty(value = "消息内容", example = "您好，有什么可以帮助您的？")
    private String content;          // 消息内容
    
    @ApiModelProperty(value = "发送者ID", example = "1")
    private Long senderId;           // 发送者ID
    
    @ApiModelProperty(value = "发送者类型", notes = "customer:用户/admin:管理员", example = "admin")
    private String senderType;       // 发送者类型（customer用户/admin管理员）
    
    @ApiModelProperty(value = "接收者ID", example = "2")
    private Long receiverId;         // 接收者ID
    
    @ApiModelProperty(value = "接收者类型", notes = "customer:用户/admin:管理员", example = "customer")
    private String receiverType;     // 接收者类型（customer用户/admin管理员）
    
    @ApiModelProperty(value = "创建时间")
    private Date createTime;         // 创建时间
    
    @ApiModelProperty(value = "消息状态", notes = "0:未读/1:已读", example = "0")
    private Integer status;          // 状态（0未读/1已读）
    
    @ApiModelProperty(value = "会话ID", example = "2c9ba0837b794f96017b7951c5e10000")
    private String sessionId;        // 会话ID
} 