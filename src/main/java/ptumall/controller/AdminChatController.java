package ptumall.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ptumall.model.ChatMessage;
import ptumall.model.ChatSession;
import ptumall.model.ChatMessageDTO;
import ptumall.service.ChatService;
import ptumall.vo.Result;
import ptumall.vo.ResultCode;

import java.util.List;

/**
 * 管理员聊天控制器
 * 负责处理管理员端的在线客服系统，提供接口用于会话管理、消息收发和状态跟踪
 * 所有接口均以/admin/api/chat为前缀，需要管理员权限
 */
@Api(tags = "管理员端在线交流接口")
@RestController
@RequestMapping("/admin/api/chat")
public class AdminChatController {

    @Autowired
    private ChatService chatService;

    /**
     * 获取管理员会话列表
     * 查询指定管理员负责的所有客户会话
     * 
     * @param adminId 管理员ID，必填参数
     * @return 管理员负责的所有会话列表
     */
    @ApiOperation(value = "获取管理员会话列表", notes = "获取指定管理员的所有交流会话")
    @GetMapping("/sessions")
    public Result<List<ChatSession>> getAdminSessions(
            @ApiParam(value = "管理员ID", required = true) @RequestParam Long adminId) {
        // 调用服务层获取管理员的会话列表，指定用户类型为"admin"
        List<ChatSession> sessions = chatService.getUserSessions(adminId, "admin");
        return Result.success(sessions);
    }

    /**
     * 获取会话消息历史
     * 查询指定会话的所有历史消息记录
     * 
     * @param sessionId 会话ID，必填参数
     * @return 会话中的所有消息列表
     */
    @ApiOperation(value = "获取会话消息历史", notes = "获取指定会话的历史消息记录")
    @GetMapping("/messages")
    public Result<List<ChatMessage>> getMessages(
            @ApiParam(value = "会话ID", required = true) @RequestParam String sessionId) {
        // 调用服务层获取会话消息历史
        List<ChatMessage> messages = chatService.getSessionMessages(sessionId);
        return Result.success(messages);
    }

    /**
     * 发送消息
     * 管理员在指定会话中发送一条消息
     * 
     * @param messageDTO 消息数据传输对象，包含消息内容、会话ID等信息
     * @return 发送成功的消息信息
     */
    @ApiOperation(value = "发送消息", notes = "在指定会话中发送一条消息")
    @PostMapping("/message")
    public Result<ChatMessage> sendMessage(
            @ApiParam(value = "消息内容", required = true) @RequestBody ChatMessageDTO messageDTO) {
        // 调用服务层发送消息，消息将保存到数据库并尝试通过WebSocket发送给客户
        ChatMessage message = chatService.sendMessage(messageDTO);
        return Result.success(message);
    }

    /**
     * 标记消息已读
     * 将单条消息标记为已读状态
     * 
     * @param messageId 消息ID，必填参数
     * @return 操作结果
     */
    @ApiOperation(value = "标记消息已读", notes = "将指定消息标记为已读状态")
    @PutMapping("/message/read")
    public Result<Void> markMessageRead(
            @ApiParam(value = "消息ID", required = true) @RequestParam Long messageId) {
        // 调用服务层标记消息已读
        chatService.markMessageAsRead(messageId);
        return Result.success();
    }

    /**
     * 标记会话所有消息已读
     * 将指定会话中的所有发送给管理员的消息标记为已读状态
     * 
     * @param sessionId 会话ID，必填参数
     * @param adminId 管理员ID，必填参数
     * @return 操作结果
     */
    @ApiOperation(value = "标记会话所有消息已读", notes = "将指定会话中的所有消息标记为已读状态")
    @PutMapping("/session/read")
    public Result<Void> markSessionRead(
            @ApiParam(value = "会话ID", required = true) @RequestParam String sessionId, 
            @ApiParam(value = "管理员ID", required = true) @RequestParam Long adminId) {
        // 调用服务层标记会话所有消息已读，指定用户类型为"admin"
        chatService.markSessionAsRead(sessionId, adminId, "admin");
        return Result.success();
    }
    
    /**
     * 结束会话
     * 将会话状态更新为已结束
     * 已结束的会话不会再接收新消息，但历史记录仍可查看
     * 
     * @param sessionId 会话ID，必填参数
     * @return 操作结果
     */
    @ApiOperation(value = "结束会话", notes = "结束指定的交流会话")
    @PutMapping("/session/end")
    public Result<Void> endSession(
            @ApiParam(value = "会话ID", required = true) @RequestParam String sessionId) {
        // 调用服务层结束会话，将会话状态更新为已结束(1)
        chatService.endSession(sessionId);
        return Result.success();
    }
    
    /**
     * 获取未读消息数量
     * 查询指定管理员的未读消息总数
     * 用于在管理员界面显示消息提醒
     * 
     * @param adminId 管理员ID，必填参数
     * @return 未读消息数量
     */
    @ApiOperation(value = "获取未读消息数量", notes = "获取指定管理员的未读消息数量")
    @GetMapping("/unread/count")
    public Result<Integer> getUnreadCount(
            @ApiParam(value = "管理员ID", required = true) @RequestParam Long adminId) {
        // 调用服务层获取未读消息数量，指定用户类型为"admin"
        int count = chatService.getUnreadCount(adminId, "admin");
        return Result.success(count);
    }
    
    /**
     * 接入新会话
     * 管理员接入一个尚未分配客服的会话
     * 接入后，会话的消息将发送给该管理员
     * 
     * @param sessionId 会话ID，必填参数
     * @param adminId 管理员ID，必填参数
     * @return 接入结果，成功返回true
     */
    @ApiOperation(value = "接入新会话", notes = "管理员接入指定的交流会话")
    @PutMapping("/session/take")
    public Result<Boolean> takeSession(
            @ApiParam(value = "会话ID", required = true) @RequestParam String sessionId, 
            @ApiParam(value = "管理员ID", required = true) @RequestParam Long adminId) {
        // 调用服务层分配会话给管理员，更新会话的adminId字段
        boolean result = chatService.assignAdminToSession(sessionId, adminId);
        return Result.success(result);
    }
    
    /**
     * 获取待接入会话列表
     * 查询所有未分配管理员的会话
     * 用于显示在管理员的待接入列表中
     * 
     * @return 待接入的会话列表
     */
    @ApiOperation(value = "获取待接入会话列表", notes = "获取所有未分配管理员的会话")
    @GetMapping("/sessions/pending")
    public Result<List<ChatSession>> getPendingSessions() {
        // 调用服务层获取所有未分配管理员的会话
        List<ChatSession> sessions = chatService.getPendingSessions();
        return Result.success(sessions);
    }
    
    /**
     * 删除会话
     * 删除指定的会话及其所有消息记录
     * 管理员可以删除任何会话，无需验证会话所属
     * 
     * @param sessionId 会话ID，路径参数
     * @param adminId 管理员ID，用于记录操作人
     * @return 删除结果，成功返回true
     */
    @ApiOperation(value = "删除会话", notes = "删除指定的会话及其所有消息记录")
    @DeleteMapping("/session/{sessionId}")
    public Result<Boolean> deleteSession(
            @ApiParam(value = "会话ID", required = true) @PathVariable String sessionId,
            @ApiParam(value = "管理员ID", required = true) @RequestParam Long adminId) {
        try {
            // 验证会话是否存在
            ChatSession session = chatService.getSessionById(sessionId);
            if (session == null) {
                return Result.failure(ResultCode.NOT_FOUND, "会话不存在");
            }
            
            // 管理员可以删除任何会话，无需验证会话所属
            // 这与用户端不同，用户只能删除自己的会话
            boolean result = chatService.deleteSession(sessionId);
            return Result.success(result);
        } catch (Exception e) {
            // 捕获并处理删除过程中的异常
            return Result.failure(ResultCode.INTERNAL_SERVER_ERROR, "删除会话失败：" + e.getMessage());
        }
    }
} 