package ptumall.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ptumall.dao.ChatMessageMapper;
import ptumall.dao.ChatSessionMapper;
import ptumall.model.ChatMessage;
import ptumall.model.ChatSession;
import ptumall.model.ChatMessageDTO;
import ptumall.service.AdminChatWebSocketServer;
import ptumall.service.ChatService;
import ptumall.service.ChatWebSocketServer;

import javax.websocket.Session;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * 聊天服务实现类
 * 实现了ChatService接口，提供在线客服系统的核心业务逻辑
 * 负责会话管理、消息收发、状态追踪等功能
 * 同时与WebSocket服务集成，实现消息的实时推送
 */
@Slf4j  // Lombok注解，自动创建日志对象log
@Service  // Spring服务注解，标识这是一个服务类
public class ChatServiceImpl implements ChatService {

    /**
     * 聊天消息数据访问接口
     * 用于操作聊天消息的数据库记录
     */
    @Autowired
    private ChatMessageMapper chatMessageMapper;

    /**
     * 聊天会话数据访问接口
     * 用于操作聊天会话的数据库记录
     */
    @Autowired
    private ChatSessionMapper chatSessionMapper;

    /**
     * JSON对象映射器
     * 用于将对象转换为JSON字符串，用在WebSocket消息传输
     */
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 创建新的聊天会话
     * 生成唯一的会话ID，设置初始状态，并保存到数据库
     *
     * @param customerId 客户ID
     * @param title 会话标题
     * @return 创建成功的会话对象
     */
    @Override
    @Transactional  // 事务注解，确保操作的原子性
    public ChatSession createSession(Long customerId, String title) {
        // 创建会话对象并设置初始属性
        ChatSession chatSession = new ChatSession();
        chatSession.setId(UUID.randomUUID().toString());  // 生成唯一的UUID作为会话ID
        chatSession.setCustomerId(customerId);  // 设置客户ID
        chatSession.setAdminId(null);  // 初始无管理员，等待分配
        chatSession.setCreateTime(new Date());  // 设置创建时间
        chatSession.setLastUpdateTime(new Date());  // 设置最后更新时间
        chatSession.setStatus(0);  // 状态为"进行中"(0)
        chatSession.setTitle(title);  // 设置会话标题
        
        // 将会话保存到数据库
        chatSessionMapper.insert(chatSession);
        log.info("创建聊天会话: {}", chatSession.getId());
        
        return chatSession;
    }

    /**
     * 发送聊天消息
     * 将消息保存到数据库，并尝试通过WebSocket实时推送给接收者
     *
     * @param messageDTO 消息数据传输对象
     * @return 保存的消息对象
     * @throws IllegalArgumentException 如果消息参数无效
     */
    @Override
    @Transactional
    public ChatMessage sendMessage(ChatMessageDTO messageDTO) {
        // 参数验证，确保必要字段不为空
        if (messageDTO.getContent() == null || messageDTO.getContent().trim().isEmpty()) {
            throw new IllegalArgumentException("消息内容不能为空");
        }
        if (messageDTO.getSenderId() == null) {
            throw new IllegalArgumentException("发送者ID不能为空");
        }
        if (messageDTO.getSenderType() == null) {
            throw new IllegalArgumentException("发送者类型不能为空");
        }
        if (messageDTO.getSessionId() == null) {
            throw new IllegalArgumentException("会话ID不能为空");
        }
        
        // 如果接收者信息为空，根据会话信息自动补充
        if (messageDTO.getReceiverId() == null || messageDTO.getReceiverType() == null) {
            // 查询会话信息
            ChatSession session = chatSessionMapper.findById(messageDTO.getSessionId());
            if (session == null) {
                throw new IllegalArgumentException("会话不存在");
            }
            
            // 根据发送者类型推断接收者类型
            if ("customer".equals(messageDTO.getSenderType())) {
                // 如果是客户发送，则接收者是管理员
                messageDTO.setReceiverType("admin");
                messageDTO.setReceiverId(session.getAdminId());
                
                // 如果会话尚未分配管理员，使用默认管理员ID
                if (messageDTO.getReceiverId() == null) {
                    messageDTO.setReceiverId(1L); // 默认系统管理员ID
                    log.info("会话{}尚未分配管理员，消息将发送给默认管理员", session.getId());
                }
            } else if ("admin".equals(messageDTO.getSenderType())) {
                // 如果是管理员发送，则接收者是客户
                messageDTO.setReceiverType("customer");
                messageDTO.setReceiverId(session.getCustomerId());
            }
        }
        
        // 最终检查接收者信息是否完整
        if (messageDTO.getReceiverId() == null) {
            throw new IllegalArgumentException("接收者ID不能为空");
        }
        if (messageDTO.getReceiverType() == null) {
            throw new IllegalArgumentException("接收者类型不能为空");
        }
        
        // 创建消息实体并保存到数据库
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setContent(messageDTO.getContent());  // 设置消息内容
        chatMessage.setSenderId(messageDTO.getSenderId());  // 设置发送者ID
        chatMessage.setSenderType(messageDTO.getSenderType());  // 设置发送者类型
        chatMessage.setReceiverId(messageDTO.getReceiverId());  // 设置接收者ID
        chatMessage.setReceiverType(messageDTO.getReceiverType());  // 设置接收者类型
        chatMessage.setCreateTime(new Date());  // 设置创建时间
        chatMessage.setStatus(0);  // 状态为"未读"(0)
        chatMessage.setSessionId(messageDTO.getSessionId());  // 设置会话ID
        
        // 将消息保存到数据库
        chatMessageMapper.insert(chatMessage);
        
        // 更新会话的最后更新时间
        chatSessionMapper.updateLastTime(messageDTO.getSessionId());
        
        // 尝试通过WebSocket实时发送消息
        try {
            // 设置时间戳到DTO用于WebSocket传输
            if (messageDTO.getTimestamp() == null) {
                messageDTO.setTimestamp(System.currentTimeMillis());
            }
            
            // 根据接收者类型选择不同的WebSocket服务
            if ("customer".equals(messageDTO.getReceiverType())) {
                // 发送给客户
                Session receiverSession = ChatWebSocketServer.getSession(
                        messageDTO.getReceiverId(), "customer");
                
                // 如果接收者在线，通过WebSocket发送
                if (receiverSession != null) {
                    ChatWebSocketServer.sendMessage(receiverSession, objectMapper.writeValueAsString(messageDTO));
                    log.debug("消息已通过WebSocket发送给客户: {}", messageDTO.getReceiverId());
                } else {
                    log.debug("客户{}不在线，消息已保存到数据库", messageDTO.getReceiverId());
                }
            } else if ("admin".equals(messageDTO.getReceiverType())) {
                // 发送给管理员
                Session receiverSession = AdminChatWebSocketServer.getSession(messageDTO.getReceiverId());
                
                // 如果接收者在线，通过WebSocket发送
                if (receiverSession != null) {
                    AdminChatWebSocketServer.sendMessage(receiverSession, objectMapper.writeValueAsString(messageDTO));
                    log.debug("消息已通过WebSocket发送给管理员: {}", messageDTO.getReceiverId());
                } else {
                    log.debug("管理员{}不在线，消息已保存到数据库", messageDTO.getReceiverId());
                }
            }
        } catch (JsonProcessingException e) {
            // 记录错误但不影响事务，消息已经保存到数据库
            log.error("消息WebSocket发送失败，但已保存到数据库", e);
        }
        
        return chatMessage;
    }

    /**
     * 获取会话的消息历史
     *
     * @param sessionId 会话ID
     * @return 会话中的所有消息列表
     */
    @Override
    public List<ChatMessage> getSessionMessages(String sessionId) {
        return chatMessageMapper.findBySessionId(sessionId);
    }

    /**
     * 获取用户的会话列表
     * 根据用户类型查询不同的会话列表
     *
     * @param userId 用户ID
     * @param userType 用户类型，"customer"表示客户，"admin"表示管理员
     * @return 用户参与的会话列表
     */
    @Override
    public List<ChatSession> getUserSessions(Long userId, String userType) {
        if ("customer".equals(userType)) {
            // 查询客户的会话列表
            return chatSessionMapper.findByCustomerId(userId);
        } else if ("admin".equals(userType)) {
            // 查询管理员的会话列表
            return chatSessionMapper.findByAdminId(userId);
        }
        return null;
    }

    /**
     * 获取用户的未读消息数量
     *
     * @param userId 用户ID
     * @param userType 用户类型
     * @return 未读消息数量
     */
    @Override
    public int getUnreadCount(Long userId, String userType) {
        List<ChatMessage> unreadMessages = chatMessageMapper.findUnreadMessages(userId, userType);
        return unreadMessages.size();
    }

    /**
     * 将单条消息标记为已读
     *
     * @param messageId 消息ID
     */
    @Override
    public void markMessageAsRead(Long messageId) {
        chatMessageMapper.markAsRead(messageId);
        log.debug("消息已标记为已读: {}", messageId);
    }

    /**
     * 将会话中的所有消息标记为已读
     *
     * @param sessionId 会话ID
     * @param userId 用户ID
     * @param userType 用户类型
     */
    @Override
    public void markSessionAsRead(String sessionId, Long userId, String userType) {
        chatMessageMapper.markAllAsRead(sessionId, userId, userType);
        log.debug("会话{}中用户{}的所有消息已标记为已读", sessionId, userId);
    }

    /**
     * 结束会话
     * 将会话状态更新为已结束
     *
     * @param sessionId 会话ID
     */
    @Override
    public void endSession(String sessionId) {
        chatSessionMapper.updateStatus(sessionId, 1); // 1表示已结束
        log.info("会话已结束: {}", sessionId);
    }

    /**
     * 分配管理员到会话
     * 将指定的管理员分配给指定的会话
     *
     * @param sessionId 会话ID
     * @param adminId 管理员ID
     * @return 是否分配成功
     */
    @Override
    @Transactional
    public boolean assignAdminToSession(String sessionId, Long adminId) {
        try {
            // 更新会话的管理员ID
            int result = chatSessionMapper.assignAdmin(sessionId, adminId);
            
            // 检查更新结果
            if (result > 0) {
                log.info("会话 {} 已分配给管理员 {}", sessionId, adminId);
                
                // 更新会话的最后更新时间
                chatSessionMapper.updateLastTime(sessionId);
                
                return true;
            } else {
                // 更新失败，可能是会话不存在或已被分配
                log.error("分配管理员失败：会话ID {} 不存在或已被分配", sessionId);
                return false;
            }
        } catch (Exception e) {
            // 捕获并记录异常
            log.error("分配管理员失败", e);
            return false;
        }
    }

    /**
     * 获取待接入的会话列表
     * 查询所有未分配管理员的会话
     *
     * @return 待接入的会话列表
     */
    @Override
    public List<ChatSession> getPendingSessions() {
        return chatSessionMapper.findPendingSessions();
    }

    /**
     * 根据ID获取会话详情
     *
     * @param sessionId 会话ID
     * @return 会话对象，如果不存在则返回null
     */
    @Override
    public ChatSession getSessionById(String sessionId) {
        return chatSessionMapper.findById(sessionId);
    }

    /**
     * 删除会话及其所有消息
     * 先删除会话中的所有消息，再删除会话本身
     *
     * @param sessionId 会话ID
     * @return 是否删除成功
     */
    @Override
    @Transactional
    public boolean deleteSession(String sessionId) {
        try {
            // 先检查会话是否存在
            ChatSession session = chatSessionMapper.findById(sessionId);
            if (session == null) {
                log.warn("删除会话失败：会话不存在，ID: {}", sessionId);
                return false;
            }
            
            // 先删除会话中的所有消息
            chatMessageMapper.deleteBySessionId(sessionId);
            log.info("已删除会话 {} 的所有消息", sessionId);
            
            // 再删除会话本身
            int result = chatSessionMapper.deleteById(sessionId);
            if (result > 0) {
                log.info("成功删除会话: {}", sessionId);
                return true;
            } else {
                // 删除失败，可能会话已被其他操作删除
                log.error("删除会话失败：会话ID {} 可能已被删除", sessionId);
                return false;
            }
        } catch (Exception e) {
            // 捕获并记录异常
            log.error("删除会话失败", e);
            return false;
        }
    }
} 