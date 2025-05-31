package ptumall.dao;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import ptumall.entity.Refund;
import java.util.List;

/**
 * 退款数据访问层
 */
@Mapper
public interface RefundDao {
    
    /**
     * 插入退款记录
     * @param refund 退款记录
     * @return 影响行数
     */
    int insert(Refund refund);
    
    /**
     * 根据ID查询退款记录
     * @param id 退款ID
     * @return 退款记录
     */
    Refund findById(Integer id);
    
    /**
     * 根据订单ID查询退款记录
     * @param orderId 订单ID
     * @return 退款记录列表
     */
    List<Refund> findByOrderId(Integer orderId);
    
    /**
     * 根据用户ID查询退款记录
     * @param userId 用户ID
     * @param status 退款状态（可选）
     * @return 退款记录列表
     */
    List<Refund> findByUserId(@Param("userId") Integer userId, @Param("status") Integer status);
    
    /**
     * 更新退款记录
     * @param refund 退款记录
     * @return 影响行数
     */
    int updateById(Refund refund);
    
    /**
     * 查询所有退款记录（管理员使用）
     * @param status 退款状态（可选）
     * @return 退款记录列表
     */
    List<Refund> findAll(@Param("status") Integer status);
} 