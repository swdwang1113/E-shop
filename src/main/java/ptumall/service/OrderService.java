package ptumall.service;

import ptumall.model.Orders;
import ptumall.vo.CreateOrderParam;
import ptumall.vo.OrderStatisticsVO;
import ptumall.vo.PageResult;

import java.util.List;

/**
 * 订单服务接口
 */
public interface OrderService {
    /**
     * 创建订单
     * @param userId 用户ID
     * @param param 创建订单参数
     * @return 订单信息
     */
    Orders createOrder(Integer userId, CreateOrderParam param);
    
    /**
     * 根据订单ID获取订单详情
     * @param userId 用户ID
     * @param orderId 订单ID
     * @return 订单信息
     */
    Orders getOrderDetail(Integer userId, Integer orderId);
    
    /**
     * 根据订单编号获取订单详情
     * @param userId 用户ID
     * @param orderNo 订单编号
     * @return 订单信息
     */
    Orders getOrderByOrderNo(Integer userId, String orderNo);
    
    /**
     * 获取用户订单列表
     * @param userId 用户ID
     * @param pageNum 页码
     * @param pageSize 每页数量
     * @return 订单列表分页结果
     */
    PageResult<Orders> getOrderList(Integer userId, Integer pageNum, Integer pageSize);
    
    /**
     * 取消订单
     * @param userId 用户ID
     * @param orderId 订单ID
     * @return 是否成功
     */
    boolean cancelOrder(Integer userId, Integer orderId);
    
    /**
     * 支付订单
     * @param userId 用户ID
     * @param orderId 订单ID
     * @param paymentType 支付方式
     * @return 是否成功
     */
    boolean payOrder(Integer userId, Integer orderId, Byte paymentType);
    
    /**
     * 确认收货
     * @param userId 用户ID
     * @param orderId 订单ID
     * @return 是否成功
     */
    boolean confirmReceipt(Integer userId, Integer orderId);
    
    /**
     * 获取所有订单列表（管理员接口）
     * @param pageNum 页码
     * @param pageSize 每页数量
     * @return 订单列表分页结果
     */
    PageResult<Orders> getAllOrderList(Integer pageNum, Integer pageSize);
    
    /**
     * 订单发货（管理员接口）
     * @param orderId 订单ID
     * @return 是否成功
     */
    boolean shipOrder(Integer orderId);
    
    /**
     * 删除订单（管理员接口）
     * @param orderId 订单ID
     * @return 是否成功
     */
    boolean deleteOrder(Integer orderId);
    
    /**
     * 删除用户自己的订单
     * @param userId 用户ID
     * @param orderId 订单ID
     * @return 是否成功
     */
    boolean deleteUserOrder(Integer userId, Integer orderId);
    
    /**
     * 管理员获取订单详情
     * @param orderId 订单ID
     * @return 订单信息
     */
    Orders getAdminOrderDetail(Integer orderId);
    
    /**
     * 获取订单统计数据（管理员接口）
     * @return 订单统计数据
     */
    OrderStatisticsVO getOrderStatistics();
}
