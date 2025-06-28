package ptumall.dao;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.apache.ibatis.annotations.Delete;
import ptumall.model.ChatMessage;

import java.util.List;

/**
 * 聊天消息Mapper接口
 */
@Mapper
public interface ChatMessageMapper {

    /**
     * 保存聊天消息
     */
    @Insert("INSERT INTO chat_message(content, sender_id, sender_type, receiver_id, receiver_type, create_time, status, session_id) " +
            "VALUES(#{content}, #{senderId}, #{senderType}, #{receiverId}, #{receiverType}, #{createTime}, #{status}, #{sessionId})")
    int insert(ChatMessage chatMessage);

    /**
     * 查询会话中的聊天记录
     */
    @Select("SELECT id, content, sender_id as senderId, sender_type as senderType, receiver_id as receiverId, " +
            "receiver_type as receiverType, create_time as createTime, status, session_id as sessionId " +
            "FROM chat_message WHERE session_id = #{sessionId} ORDER BY create_time ASC")
    List<ChatMessage> findBySessionId(@Param("sessionId") String sessionId);

    /**
     * 查询未读消息
     */
    @Select("SELECT id, content, sender_id as senderId, sender_type as senderType, receiver_id as receiverId, " +
            "receiver_type as receiverType, create_time as createTime, status, session_id as sessionId " +
            "FROM chat_message WHERE receiver_id = #{receiverId} AND receiver_type = #{receiverType} AND status = 0")
    List<ChatMessage> findUnreadMessages(@Param("receiverId") Long receiverId, @Param("receiverType") String receiverType);
    
    /**
     * 将消息标记为已读
     */
    @Update("UPDATE chat_message SET status = 1 WHERE id = #{id}")
    int markAsRead(@Param("id") Long id);
    
    /**
     * 将会话中的所有消息标记为已读
     */
    @Update("UPDATE chat_message SET status = 1 WHERE session_id = #{sessionId} AND receiver_id = #{receiverId} AND receiver_type = #{receiverType}")
    int markAllAsRead(@Param("sessionId") String sessionId, @Param("receiverId") Long receiverId, @Param("receiverType") String receiverType);
    
    /**
     * 删除会话中的所有消息
     */
    @Delete("DELETE FROM chat_message WHERE session_id = #{sessionId}")
    int deleteBySessionId(@Param("sessionId") String sessionId);
} 