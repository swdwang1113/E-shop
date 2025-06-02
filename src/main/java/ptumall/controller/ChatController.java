package ptumall.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ptumall.config.JWTInterceptors;
import ptumall.entity.ChatMessage;
import ptumall.entity.ChatSession;
import ptumall.model.ChatMessageDTO;
import ptumall.model.ChatSessionDTO;
import ptumall.service.ChatService;
import ptumall.vo.Result;
import ptumall.vo.ResultCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.List;

/**
 * 用户聊天控制器
 */
@Api(tags = "用户端在线交流接口")
@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private static final Logger log = LoggerFactory.getLogger(ChatController.class);

    @Autowired
    private ChatService chatService;

    /**
     * 创建会话
     */
    @ApiOperation(value = "创建交流会话", notes = "用户创建一个新的交流会话")
    @PostMapping("/session")
    public Result<ChatSession> createSession(@RequestBody ChatSessionDTO sessionDTO, HttpServletRequest request) {
        // 从JWT中获取用户ID
        Object userIdObj = request.getAttribute(JWTInterceptors.USER_ID_KEY);
        Long userId = userIdObj != null ? Long.valueOf(userIdObj.toString()) : null;
        
        // 如果前端传递了用户ID，则使用前端传递的ID，否则使用JWT中的ID
        Long customerId = sessionDTO.getCustomerId() != null ? sessionDTO.getCustomerId() : userId;
        
        // 验证用户ID是否存在
        if (customerId == null) {
            return Result.validateFailed("用户ID不能为空");
        }
        
        ChatSession session = chatService.createSession(customerId, sessionDTO.getTitle());
        return Result.success(session);
    }

    /**
     * 获取用户会话列表
     */
    @ApiOperation(value = "获取用户会话列表", notes = "获取指定用户的所有交流会话")
    @GetMapping("/sessions")
    public Result<List<ChatSession>> getUserSessions(
            @ApiParam(value = "用户ID", required = true) @RequestParam Long userId) {
        List<ChatSession> sessions = chatService.getUserSessions(userId, "customer");
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
        if (messages != null && !messages.isEmpty()) {
            for (ChatMessage message : messages) {
                if (message.getStatus() == null) {
                    message.setStatus(0);
                }
                if (message.getCreateTime() == null) {
                    message.setCreateTime(new Date());
                }
            }
        }
        return Result.success(messages);
    }

    /**
     * 发送消息
     */
    @ApiOperation(value = "发送消息", notes = "在指定会话中发送一条消息")
    @PostMapping("/message")
    public Result<ChatMessage> sendMessage(
            @ApiParam(value = "消息内容", required = true) @RequestBody ChatMessageDTO messageDTO, 
            HttpServletRequest request) {
        try {
            // 从JWT中获取用户ID
            Object userIdObj = request.getAttribute(JWTInterceptors.USER_ID_KEY);
            Long userId = userIdObj != null ? Long.valueOf(userIdObj.toString()) : null;
            
            // 如果前端没有传入发送者ID，则使用JWT中的用户ID
            if (messageDTO.getSenderId() == null) {
                messageDTO.setSenderId(userId);
                messageDTO.setSenderType("customer"); // 默认为客户发送
            }
            
            ChatMessage message = chatService.sendMessage(messageDTO);
            return Result.success(message);
        } catch (IllegalArgumentException e) {
            return Result.validateFailed(e.getMessage());
        } catch (Exception e) {
            log.error("发送消息失败", e);
            return Result.failure(ResultCode.INTERNAL_SERVER_ERROR, "发送消息失败：" + e.getMessage());
        }
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
            @ApiParam(value = "用户ID", required = true) @RequestParam Long userId) {
        chatService.markSessionAsRead(sessionId, userId, "customer");
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
    @ApiOperation(value = "获取未读消息数量", notes = "获取指定用户的未读消息数量")
    @GetMapping("/unread/count")
    public Result<Integer> getUnreadCount(
            @ApiParam(value = "用户ID", required = true) @RequestParam Long userId) {
        int count = chatService.getUnreadCount(userId, "customer");
        return Result.success(count);
    }
    
    /**
     * 删除会话
     */
    @ApiOperation(value = "删除会话", notes = "删除指定的会话及其所有消息记录")
    @DeleteMapping("/session/{sessionId}")
    public Result<Boolean> deleteSession(
            @ApiParam(value = "会话ID", required = true) @PathVariable String sessionId,
            HttpServletRequest request) {
        try {
            // 从JWT中获取用户ID
            Object userIdObj = request.getAttribute(JWTInterceptors.USER_ID_KEY);
            Long userId = userIdObj != null ? Long.valueOf(userIdObj.toString()) : null;
            
            // 验证权限：检查会话是否属于当前用户
            ChatSession session = chatService.getSessionById(sessionId);
            if (session == null) {
                return Result.failure(ResultCode.NOT_FOUND, "会话不存在");
            }
            
            if (!session.getCustomerId().equals(userId)) {
                return Result.failure(ResultCode.FORBIDDEN, "无权删除此会话");
            }
            
            boolean result = chatService.deleteSession(sessionId);
            return Result.success(result);
        } catch (Exception e) {
            log.error("删除会话失败", e);
            return Result.failure(ResultCode.INTERNAL_SERVER_ERROR, "删除会话失败：" + e.getMessage());
        }
    }
} 