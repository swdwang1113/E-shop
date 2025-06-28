package ptumall.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ptumall.config.JWTInterceptors;
import ptumall.model.ChatMessage;
import ptumall.model.ChatSession;
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
 * 负责处理用户端的在线交流功能，包括创建会话、发送消息、查询历史等操作
 * 所有接口均以/api/chat为前缀
 */
@Api(tags = "用户端在线交流接口")
@RestController
@RequestMapping("/api/chat")
public class ChatController {

    /**
     * 日志记录器
     * 用于记录聊天控制器中的操作日志、异常和调试信息
     * 便于系统运行时的问题排查和监控
     */
    private static final Logger log = LoggerFactory.getLogger(ChatController.class);

    @Autowired
    private ChatService chatService;

    /**
     * 创建会话
     * 用户可以通过此接口创建一个新的客服会话
     * 
     * @param sessionDTO 会话数据传输对象，包含会话标题等信息
     * @param request HTTP请求对象，用于获取JWT中的用户信息
     * @return 创建成功的会话信息
     */
    @ApiOperation(value = "创建交流会话", notes = "用户创建一个新的交流会话")
    @PostMapping("/session")
    public Result<ChatSession> createSession(@RequestBody ChatSessionDTO sessionDTO, HttpServletRequest request) {
        // 从JWT中获取用户ID
        Object userIdObj = request.getAttribute(JWTInterceptors.USER_ID_KEY);
        // 将Object类型的userId转换为Long类型，如果为null则保持为null
        Long userId = userIdObj != null ? Long.valueOf(userIdObj.toString()) : null;
        
        // 如果前端传递了用户ID，则使用前端传递的ID，否则使用JWT中的ID
        // 这提供了灵活性，允许系统代表特定用户创建会话
        Long customerId = sessionDTO.getCustomerId() != null ? sessionDTO.getCustomerId() : userId;
        
        // 验证用户ID是否存在，确保会话有有效的所有者
        if (customerId == null) {
            return Result.validateFailed("用户ID不能为空");
        }
        
        // 调用服务层创建会话
        ChatSession session = chatService.createSession(customerId, sessionDTO.getTitle());
        return Result.success(session);
    }

    /**
     * 获取用户会话列表
     * 返回指定用户参与的所有聊天会话
     * 
     * @param userId 用户ID
     * @return 用户的会话列表
     */
    @ApiOperation(value = "获取用户会话列表", notes = "获取指定用户的所有交流会话")
    @GetMapping("/sessions")
    public Result<List<ChatSession>> getUserSessions(
            @ApiParam(value = "用户ID", required = true) @RequestParam Long userId) {
        // 调用服务层获取用户会话列表，指定用户类型为"customer"
        List<ChatSession> sessions = chatService.getUserSessions(userId, "customer");
        return Result.success(sessions);
    }

    /**
     * 获取会话消息历史
     * 返回指定会话的所有历史消息记录
     * 
     * @param sessionId 会话ID
     * @return 会话中的消息列表
     */
    @ApiOperation(value = "获取会话消息历史", notes = "获取指定会话的历史消息记录")
    @GetMapping("/messages")
    public Result<List<ChatMessage>> getMessages(
            @ApiParam(value = "会话ID", required = true) @RequestParam String sessionId) {
        // 调用服务层获取会话消息历史
        List<ChatMessage> messages = chatService.getSessionMessages(sessionId);
        if (messages != null && !messages.isEmpty()) {
            // 处理消息列表，确保每条消息都有正确的状态和创建时间
            // 这是为了防止数据库中的空值影响前端显示
            for (ChatMessage message : messages) {
                if (message.getStatus() == null) {
                    message.setStatus(0); // 默认为未读状态
                }
                if (message.getCreateTime() == null) {
                    message.setCreateTime(new Date()); // 设置当前时间作为创建时间
                }
            }
        }
        return Result.success(messages);
    }

    /**
     * 发送消息
     * 在指定会话中发送一条新消息
     * 
     * @param messageDTO 消息数据传输对象，包含消息内容、会话ID等信息
     * @param request HTTP请求对象，用于获取JWT中的用户信息
     * @return 发送成功的消息信息
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
            // 这确保了消息始终有一个有效的发送者
            if (messageDTO.getSenderId() == null) {
                messageDTO.setSenderId(userId);
                messageDTO.setSenderType("customer"); // 默认为客户发送
            }
            
            // 调用服务层发送消息
            ChatMessage message = chatService.sendMessage(messageDTO);
            return Result.success(message);
        } catch (IllegalArgumentException e) {
            // 参数验证失败，如内容为空、会话ID不存在等
            return Result.validateFailed(e.getMessage());
        } catch (Exception e) {
            // 记录其他未预期的异常
            log.error("发送消息失败", e);
            return Result.failure(ResultCode.INTERNAL_SERVER_ERROR, "发送消息失败：" + e.getMessage());
        }
    }

    /**
     * 标记消息已读
     * 将单条消息标记为已读状态
     * 
     * @param messageId 消息ID
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
     * 将指定会话中的所有消息标记为已读状态
     * 
     * @param sessionId 会话ID
     * @param userId 用户ID
     * @return 操作结果
     */
    @ApiOperation(value = "标记会话所有消息已读", notes = "将指定会话中的所有消息标记为已读状态")
    @PutMapping("/session/read")
    public Result<Void> markSessionRead(
            @ApiParam(value = "会话ID", required = true) @RequestParam String sessionId, 
            @ApiParam(value = "用户ID", required = true) @RequestParam Long userId) {
        // 调用服务层标记会话所有消息已读，指定用户类型为"customer"
        chatService.markSessionAsRead(sessionId, userId, "customer");
        return Result.success();
    }
    
    /**
     * 结束会话
     * 将会话状态更新为已结束
     * 
     * @param sessionId 会话ID
     * @return 操作结果
     */
    @ApiOperation(value = "结束会话", notes = "结束指定的交流会话")
    @PutMapping("/session/end")
    public Result<Void> endSession(
            @ApiParam(value = "会话ID", required = true) @RequestParam String sessionId) {
        // 调用服务层结束会话
        chatService.endSession(sessionId);
        return Result.success();
    }
    
    /**
     * 获取未读消息数量
     * 返回指定用户的未读消息总数
     * 
     * @param userId 用户ID
     * @return 未读消息数量
     */
    @ApiOperation(value = "获取未读消息数量", notes = "获取指定用户的未读消息数量")
    @GetMapping("/unread/count")
    public Result<Integer> getUnreadCount(
            @ApiParam(value = "用户ID", required = true) @RequestParam Long userId) {
        // 调用服务层获取未读消息数量，指定用户类型为"customer"
        int count = chatService.getUnreadCount(userId, "customer");
        return Result.success(count);
    }
    
    /**
     * 删除会话
     * 删除指定的会话及其所有消息记录
     * 
     * @param sessionId 会话ID
     * @param request HTTP请求对象，用于获取JWT中的用户信息
     * @return 操作结果
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
            
            // 验证权限：检查会话是否存在
            ChatSession session = chatService.getSessionById(sessionId);
            if (session == null) {
                return Result.failure(ResultCode.NOT_FOUND, "会话不存在");
            }
            
            // 验证权限：检查会话是否属于当前用户
            // 只有会话的创建者才能删除会话
            if (!session.getCustomerId().equals(userId)) {
                return Result.failure(ResultCode.FORBIDDEN, "无权删除此会话");
            }
            
            // 调用服务层删除会话
            boolean result = chatService.deleteSession(sessionId);
            return Result.success(result);
        } catch (Exception e) {
            // 记录删除失败的异常
            log.error("删除会话失败", e);
            return Result.failure(ResultCode.INTERNAL_SERVER_ERROR, "删除会话失败：" + e.getMessage());
        }
    }
} 