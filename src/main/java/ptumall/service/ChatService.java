package ptumall.service;

import ptumall.model.ChatMessage;
import ptumall.model.ChatSession;
import ptumall.model.ChatMessageDTO;

import java.util.List;

/**
 * 聊天服务接口
 */
public interface ChatService {

    /**
     * 创建聊天会话
     */
    ChatSession createSession(Long customerId, String title);

    /**
     * 发送消息
     */
    ChatMessage sendMessage(ChatMessageDTO messageDTO);

    /**
     * 获取会话消息历史
     */
    List<ChatMessage> getSessionMessages(String sessionId);

    /**
     * 获取用户会话列表
     */
    List<ChatSession> getUserSessions(Long userId, String userType);

    /**
     * 获取未读消息数
     */
    int getUnreadCount(Long userId, String userType);

    /**
     * 标记消息为已读
     */
    void markMessageAsRead(Long messageId);

    /**
     * 标记会话所有消息为已读
     */
    void markSessionAsRead(String sessionId, Long userId, String userType);

    /**
     * 结束会话
     */
    void endSession(String sessionId);
    
    /**
     * 分配管理员到会话
     */
    boolean assignAdminToSession(String sessionId, Long adminId);
    
    /**
     * 获取待接入的会话列表
     * 返回所有未分配管理员的会话
     */
    List<ChatSession> getPendingSessions();
    
    /**
     * 根据ID获取会话详情
     * 
     * @param sessionId 会话ID
     * @return 会话详情，如果不存在则返回null
     */
    ChatSession getSessionById(String sessionId);
    
    /**
     * 删除会话
     * 删除会话及其所有相关消息记录
     * 
     * @param sessionId 会话ID
     * @return 删除是否成功
     */
    boolean deleteSession(String sessionId);
} 