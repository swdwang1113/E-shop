package ptumall.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 聊天消息传输对象
 */
@Data
@ApiModel(description = "聊天消息传输对象")
public class ChatMessageDTO {
    @ApiModelProperty(value = "消息类型", notes = "CHAT:聊天消息/JOIN:加入会话/LEAVE:离开会话/TYPING:正在输入", example = "CHAT")
    private String type;             // 消息类型（CHAT/JOIN/LEAVE/TYPING）
    
    @ApiModelProperty(value = "消息内容", example = "您好，有什么需要帮助的吗？")
    private String content;          // 消息内容
    
    @ApiModelProperty(value = "发送者ID", example = "1")
    private Long senderId;           // 发送者ID
    
    @ApiModelProperty(value = "发送者类型", notes = "customer:用户/admin:管理员", example = "admin")
    private String senderType;       // 发送者类型
    
    @ApiModelProperty(value = "接收者ID", example = "2")
    private Long receiverId;         // 接收者ID
    
    @ApiModelProperty(value = "接收者类型", notes = "customer:用户/admin:管理员", example = "customer")
    private String receiverType;     // 接收者类型
    
    @ApiModelProperty(value = "发送者名称", example = "客服小王")
    private String senderName;       // 发送者名称
    
    @ApiModelProperty(value = "会话ID", example = "2c9ba0837b794f96017b7951c5e10000")
    private String sessionId;        // 会话ID
    
    @ApiModelProperty(value = "时间戳", example = "1628149845000")
    private Long timestamp;          // 时间戳
} 