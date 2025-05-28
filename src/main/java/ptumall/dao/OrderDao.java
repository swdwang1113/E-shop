package ptumall.dao;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import ptumall.model.Orders;
import ptumall.model.OrderItems;

import java.math.BigDecimal;
import java.util.List;

@Mapper
public interface OrderDao {
    /**
     * 创建订单
     * @param order 订单信息
     * @return 影响行数
     */
    int insert(Orders order);
    
    /**
     * 创建订单商品
     * @param orderItem 订单商品信息
     * @return 影响行数
     */
    int insertOrderItem(OrderItems orderItem);
    
    /**
     * 批量创建订单商品
     * @param orderItems 订单商品列表
     * @return 影响行数
     */
    int batchInsertOrderItems(List<OrderItems> orderItems);
    
    /**
     * 根据订单ID查询订单
     * @param id 订单ID
     * @return 订单信息
     */
    Orders selectById(Integer id);
    
    /**
     * 根据订单编号查询订单
     * @param orderNo 订单编号
     * @return 订单信息
     */
    Orders selectByOrderNo(String orderNo);
    
    /**
     * 根据用户ID查询订单列表
     * @param userId 用户ID
     * @return 订单列表
     */
    List<Orders> selectByUserId(Integer userId);
    
    /**
     * 根据用户ID和订单状态查询订单列表
     * @param userId 用户ID
     * @param status 订单状态
     * @return 订单列表
     */
    List<Orders> selectByUserIdAndStatus(@Param("userId") Integer userId, @Param("status") Byte status);
    
    /**
     * 根据订单ID查询订单商品
     * @param orderId 订单ID
     * @return 订单商品列表
     */
    List<OrderItems> selectOrderItemsByOrderId(Integer orderId);
    
    /**
     * 更新订单状态
     * @param id 订单ID
     * @param status 订单状态
     * @return 影响行数
     */
    int updateStatus(@Param("id") Integer id, @Param("status") Byte status);
    
    /**
     * 支付订单
     * @param id 订单ID
     * @param paymentType 支付方式
     * @return 影响行数
     */
    int pay(@Param("id") Integer id, @Param("paymentType") Byte paymentType);
    
    /**
     * 获取用户订单数量
     * @param userId 用户ID
     * @return 订单数量
     */
    int countByUserId(Integer userId);
    
    /**
     * 查询所有订单
     * @return 订单列表
     */
    List<Orders> selectAll();
    
    /**
     * 根据订单状态查询所有订单
     * @param status 订单状态
     * @return 订单列表
     */
    List<Orders> selectAllByStatus(Byte status);
    
    /**
     * 根据订单号模糊查询订单
     * @param orderNo 订单号关键词
     * @return 订单列表
     */
    List<Orders> selectByOrderNoKeyword(String orderNo);
    
    /**
     * 根据用户ID查询订单
     * @param userId 用户ID
     * @return 订单列表
     */
    List<Orders> selectByUserIdAdmin(Integer userId);
    
    /**
     * 根据订单号关键词和状态查询订单
     * @param orderNo 订单号关键词
     * @param status 订单状态
     * @return 订单列表
     */
    List<Orders> selectByOrderNoAndStatus(@Param("orderNo") String orderNo, @Param("status") Byte status);
    
    /**
     * 根据用户ID和状态查询订单
     * @param userId 用户ID
     * @param status 订单状态
     * @return 订单列表
     */
    List<Orders> selectByUserIdAndStatusAdmin(@Param("userId") Integer userId, @Param("status") Byte status);
    
    /**
     * 删除订单商品
     * @param orderId 订单ID
     * @return 影响行数
     */
    int deleteOrderItems(Integer orderId);
    
    /**
     * 删除订单
     * @param id 订单ID
     * @return 影响行数
     */
    int deleteById(Integer id);
    
    /**
     * 获取订单总数
     * @return 订单总数
     */
    int count();
    
    /**
     * 获取销售总额
     * @return 销售总额
     */
    BigDecimal sumTotalAmount();
}
