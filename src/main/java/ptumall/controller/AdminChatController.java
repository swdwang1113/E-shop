package ptumall.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ptumall.entity.ChatMessage;
import ptumall.entity.ChatSession;
import ptumall.model.ChatMessageDTO;
import ptumall.service.ChatService;
import ptumall.vo.Result;
import ptumall.vo.ResultCode;

import java.util.List;

/**
 * 管理员聊天控制器
 */
@Api(tags = "管理员端在线交流接口")
@RestController
@RequestMapping("/admin/api/chat")
public class AdminChatController {

    @Autowired
    private ChatService chatService;

    /**
     * 获取管理员会话列表
     */
    @ApiOperation(value = "获取管理员会话列表", notes = "获取指定管理员的所有交流会话")
    @GetMapping("/sessions")
    public Result<List<ChatSession>> getAdminSessions(
            @ApiParam(value = "管理员ID", required = true) @RequestParam Long adminId) {
        List<ChatSession> sessions = chatService.getUserSessions(adminId, "admin");
        return Result.success(sessions);
    }

    /**
     * 获取会话消息历史
     */
    @ApiOperation(value = "获取会话消息历史", notes = "获取指定会话的历史消息记录")
    @GetMapping("/messages")
    public Result<List<ChatMessage>> getMessages(
            @ApiParam(value = "会话ID", required = true) @RequestParam String sessionId) {
        List<ChatMessage> messages = chatService.getSessionMessages(sessionId);
        return Result.success(messages);
    }

    /**
     * 发送消息
     */
    @ApiOperation(value = "发送消息", notes = "在指定会话中发送一条消息")
    @PostMapping("/message")
    public Result<ChatMessage> sendMessage(
            @ApiParam(value = "消息内容", required = true) @RequestBody ChatMessageDTO messageDTO) {
        ChatMessage message = chatService.sendMessage(messageDTO);
        return Result.success(message);
    }

    /**
     * 标记消息已读
     */
    @ApiOperation(value = "标记消息已读", notes = "将指定消息标记为已读状态")
    @PutMapping("/message/read")
    public Result<Void> markMessageRead(
            @ApiParam(value = "消息ID", required = true) @RequestParam Long messageId) {
        chatService.markMessageAsRead(messageId);
        return Result.success();
    }

    /**
     * 标记会话所有消息已读
     */
    @ApiOperation(value = "标记会话所有消息已读", notes = "将指定会话中的所有消息标记为已读状态")
    @PutMapping("/session/read")
    public Result<Void> markSessionRead(
            @ApiParam(value = "会话ID", required = true) @RequestParam String sessionId, 
            @ApiParam(value = "管理员ID", required = true) @RequestParam Long adminId) {
        chatService.markSessionAsRead(sessionId, adminId, "admin");
        return Result.success();
    }

    /**
     * 结束会话
     */
    @ApiOperation(value = "结束会话", notes = "结束指定的交流会话")
    @PutMapping("/session/end")
    public Result<Void> endSession(
            @ApiParam(value = "会话ID", required = true) @RequestParam String sessionId) {
        chatService.endSession(sessionId);
        return Result.success();
    }
    
    /**
     * 获取未读消息数量
     */
    @ApiOperation(value = "获取未读消息数量", notes = "获取指定管理员的未读消息数量")
    @GetMapping("/unread/count")
    public Result<Integer> getUnreadCount(
            @ApiParam(value = "管理员ID", required = true) @RequestParam Long adminId) {
        int count = chatService.getUnreadCount(adminId, "admin");
        return Result.success(count);
    }
    
    /**
     * 接入新会话
     */
    @ApiOperation(value = "接入新会话", notes = "管理员接入指定的交流会话")
    @PutMapping("/session/take")
    public Result<Boolean> takeSession(
            @ApiParam(value = "会话ID", required = true) @RequestParam String sessionId, 
            @ApiParam(value = "管理员ID", required = true) @RequestParam Long adminId) {
        boolean result = chatService.assignAdminToSession(sessionId, adminId);
        return Result.success(result);
    }
    
    /**
     * 获取待接入会话列表
     */
    @ApiOperation(value = "获取待接入会话列表", notes = "获取所有未分配管理员的会话")
    @GetMapping("/sessions/pending")
    public Result<List<ChatSession>> getPendingSessions() {
        List<ChatSession> sessions = chatService.getPendingSessions();
        return Result.success(sessions);
    }
    
    /**
     * 删除会话
     */
    @ApiOperation(value = "删除会话", notes = "删除指定的会话及其所有消息记录")
    @DeleteMapping("/session/{sessionId}")
    public Result<Boolean> deleteSession(
            @ApiParam(value = "会话ID", required = true) @PathVariable String sessionId,
            @ApiParam(value = "管理员ID", required = true) @RequestParam Long adminId) {
        try {
            // 验证权限：检查会话是否存在
            ChatSession session = chatService.getSessionById(sessionId);
            if (session == null) {
                return Result.failure(ResultCode.NOT_FOUND, "会话不存在");
            }
            
            // 管理员可以删除任何会话，无需验证会话所属
            boolean result = chatService.deleteSession(sessionId);
            return Result.success(result);
        } catch (Exception e) {
            return Result.failure(ResultCode.INTERNAL_SERVER_ERROR, "删除会话失败：" + e.getMessage());
        }
    }
} 