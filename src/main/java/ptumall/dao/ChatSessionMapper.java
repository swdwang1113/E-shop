package ptumall.dao;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.apache.ibatis.annotations.Delete;
import ptumall.model.ChatSession;

import java.util.List;

/**
 * 聊天会话Mapper接口
 */
@Mapper
public interface ChatSessionMapper {

    /**
     * 创建聊天会话
     */
    @Insert("INSERT INTO chat_session(id, customer_id, admin_id, create_time, last_update_time, status, title) " +
            "VALUES(#{id}, #{customerId}, #{adminId}, #{createTime}, #{lastUpdateTime}, #{status}, #{title})")
    int insert(ChatSession chatSession);

    /**
     * 根据ID查询会话
     */
    @Select("SELECT id, customer_id as customerId, admin_id as adminId, IFNULL(create_time, NOW()) as createTime, " +
            "IFNULL(last_update_time, NOW()) as lastUpdateTime, status, title " +
            "FROM chat_session WHERE id = #{id}")
    ChatSession findById(@Param("id") String id);

    /**
     * 查询用户的所有会话
     */
    @Select("SELECT id, customer_id as customerId, admin_id as adminId, IFNULL(create_time, NOW()) as createTime, " +
            "IFNULL(last_update_time, NOW()) as lastUpdateTime, status, title " +
            "FROM chat_session WHERE customer_id = #{customerId} ORDER BY last_update_time DESC")
    List<ChatSession> findByCustomerId(@Param("customerId") Long customerId);

    /**
     * 查询管理员的所有会话
     */
    @Select("SELECT id, customer_id as customerId, admin_id as adminId, IFNULL(create_time, NOW()) as createTime, " +
            "IFNULL(last_update_time, NOW()) as lastUpdateTime, status, title " +
            "FROM chat_session WHERE admin_id = #{adminId} ORDER BY last_update_time DESC")
    List<ChatSession> findByAdminId(@Param("adminId") Long adminId);

    /**
     * 更新会话状态
     */
    @Update("UPDATE chat_session SET status = #{status}, last_update_time = NOW() WHERE id = #{id}")
    int updateStatus(@Param("id") String id, @Param("status") Integer status);

    /**
     * 更新会话的管理员ID
     */
    @Update("UPDATE chat_session SET admin_id = #{adminId}, status = 0, last_update_time = NOW() WHERE id = #{id}")
    int assignAdmin(@Param("id") String id, @Param("adminId") Long adminId);
    
    /**
     * 查询等待分配的会话
     */
    @Select("SELECT id, customer_id as customerId, admin_id as adminId, IFNULL(create_time, NOW()) as createTime, " +
            "IFNULL(last_update_time, NOW()) as lastUpdateTime, status, title " +
            "FROM chat_session WHERE admin_id IS NULL AND status = 0 ORDER BY create_time ASC")
    List<ChatSession> findPendingSessions();
    
    /**
     * 更新会话最后更新时间
     */
    @Update("UPDATE chat_session SET last_update_time = NOW() WHERE id = #{id}")
    int updateLastTime(@Param("id") String id);
    
    /**
     * 删除会话
     */
    @Delete("DELETE FROM chat_session WHERE id = #{id}")
    int deleteById(@Param("id") String id);
} 