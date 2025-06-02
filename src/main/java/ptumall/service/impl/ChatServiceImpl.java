package ptumall.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ptumall.dao.ChatMessageMapper;
import ptumall.dao.ChatSessionMapper;
import ptumall.entity.ChatMessage;
import ptumall.entity.ChatSession;
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
 */
@Slf4j
@Service
public class ChatServiceImpl implements ChatService {

    @Autowired
    private ChatMessageMapper chatMessageMapper;

    @Autowired
    private ChatSessionMapper chatSessionMapper;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    @Transactional
    public ChatSession createSession(Long customerId, String title) {
        // 创建会话
        ChatSession chatSession = new ChatSession();
        chatSession.setId(UUID.randomUUID().toString());
        chatSession.setCustomerId(customerId);
        chatSession.setAdminId(null);  // 初始无管理员
        chatSession.setCreateTime(new Date());
        chatSession.setLastUpdateTime(new Date());
        chatSession.setStatus(0);  // 进行中
        chatSession.setTitle(title);
        
        chatSessionMapper.insert(chatSession);
        log.info("创建聊天会话: {}", chatSession.getId());
        
        return chatSession;
    }

    @Override
    @Transactional
    public ChatMessage sendMessage(ChatMessageDTO messageDTO) {
        // 参数验证
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
        
        // 如果receiverId为空，需要查询会话信息自动补充接收者信息
        if (messageDTO.getReceiverId() == null || messageDTO.getReceiverType() == null) {
            ChatSession session = chatSessionMapper.findById(messageDTO.getSessionId());
            if (session == null) {
                throw new IllegalArgumentException("会话不存在");
            }
            
            // 根据发送者类型推断接收者类型
            if ("customer".equals(messageDTO.getSenderType())) {
                // 客户发送，接收者为管理员
                messageDTO.setReceiverType("admin");
                messageDTO.setReceiverId(session.getAdminId());
                
                // 如果会话尚未分配管理员，则默认为系统管理员ID 1
                if (messageDTO.getReceiverId() == null) {
                    messageDTO.setReceiverId(1L); // 默认系统管理员ID
                }
            } else if ("admin".equals(messageDTO.getSenderType())) {
                // 管理员发送，接收者为客户
                messageDTO.setReceiverType("customer");
                messageDTO.setReceiverId(session.getCustomerId());
            }
        }
        
        // 最终检查接收者信息
        if (messageDTO.getReceiverId() == null) {
            throw new IllegalArgumentException("接收者ID不能为空");
        }
        if (messageDTO.getReceiverType() == null) {
            throw new IllegalArgumentException("接收者类型不能为空");
        }
        
        // 保存消息到数据库
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setContent(messageDTO.getContent());
        chatMessage.setSenderId(messageDTO.getSenderId());
        chatMessage.setSenderType(messageDTO.getSenderType());
        chatMessage.setReceiverId(messageDTO.getReceiverId());
        chatMessage.setReceiverType(messageDTO.getReceiverType());
        chatMessage.setCreateTime(new Date());  // 确保设置当前时间
        chatMessage.setStatus(0);  // 未读
        chatMessage.setSessionId(messageDTO.getSessionId());
        
        chatMessageMapper.insert(chatMessage);
        
        // 更新会话最后更新时间
        chatSessionMapper.updateLastTime(messageDTO.getSessionId());
        
        // 尝试实时发送消息
        try {
            // 设置时间戳到DTO用于WebSocket传输
            if (messageDTO.getTimestamp() == null) {
                messageDTO.setTimestamp(System.currentTimeMillis());
            }
            
            if ("customer".equals(messageDTO.getReceiverType())) {
                // 发送给客户
                Session receiverSession = ChatWebSocketServer.getSession(
                        messageDTO.getReceiverId(), "customer");
                
                if (receiverSession != null) {
                    ChatWebSocketServer.sendMessage(receiverSession, objectMapper.writeValueAsString(messageDTO));
                }
            } else if ("admin".equals(messageDTO.getReceiverType())) {
                // 发送给管理员
                Session receiverSession = AdminChatWebSocketServer.getSession(messageDTO.getReceiverId());
                
                if (receiverSession != null) {
                    AdminChatWebSocketServer.sendMessage(receiverSession, objectMapper.writeValueAsString(messageDTO));
                }
            }
        } catch (JsonProcessingException e) {
            log.error("消息发送失败", e);
        }
        
        return chatMessage;
    }

    @Override
    public List<ChatMessage> getSessionMessages(String sessionId) {
        return chatMessageMapper.findBySessionId(sessionId);
    }

    @Override
    public List<ChatSession> getUserSessions(Long userId, String userType) {
        if ("customer".equals(userType)) {
            return chatSessionMapper.findByCustomerId(userId);
        } else if ("admin".equals(userType)) {
            return chatSessionMapper.findByAdminId(userId);
        }
        return null;
    }

    @Override
    public int getUnreadCount(Long userId, String userType) {
        List<ChatMessage> unreadMessages = chatMessageMapper.findUnreadMessages(userId, userType);
        return unreadMessages.size();
    }

    @Override
    public void markMessageAsRead(Long messageId) {
        chatMessageMapper.markAsRead(messageId);
    }

    @Override
    public void markSessionAsRead(String sessionId, Long userId, String userType) {
        chatMessageMapper.markAllAsRead(sessionId, userId, userType);
    }

    @Override
    public void endSession(String sessionId) {
        chatSessionMapper.updateStatus(sessionId, 1); // 1表示已结束
    }

    @Override
    @Transactional
    public boolean assignAdminToSession(String sessionId, Long adminId) {
        try {
            // 更新会话的管理员ID
            int result = chatSessionMapper.assignAdmin(sessionId, adminId);
            
            if (result > 0) {
                log.info("会话 {} 已分配给管理员 {}", sessionId, adminId);
                
                // 更新会话的最后更新时间
                chatSessionMapper.updateLastTime(sessionId);
                
                return true;
            } else {
                log.error("分配管理员失败：会话ID {} 不存在或已被分配", sessionId);
                return false;
            }
        } catch (Exception e) {
            log.error("分配管理员失败", e);
            return false;
        }
    }

    @Override
    public List<ChatSession> getPendingSessions() {
        return chatSessionMapper.findPendingSessions();
    }

    @Override
    public ChatSession getSessionById(String sessionId) {
        return chatSessionMapper.findById(sessionId);
    }

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
            
            // 再删除会话
            int result = chatSessionMapper.deleteById(sessionId);
            if (result > 0) {
                log.info("成功删除会话: {}", sessionId);
                return true;
            } else {
                log.error("删除会话失败：会话ID {} 可能已被删除", sessionId);
                return false;
            }
        } catch (Exception e) {
            log.error("删除会话失败", e);
            return false;
        }
    }
} 