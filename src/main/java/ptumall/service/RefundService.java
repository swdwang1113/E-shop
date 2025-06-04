package ptumall.service;

import ptumall.entity.Refund;
import java.util.List;

/**
 * 退款服务接口
 */
public interface RefundService {
    
    /**
     * 申请退款
     * @param orderId 订单ID
     * @param userId 用户ID
     * @param reason 退款原因
     * @param description 详细描述
     * @return 退款记录
     */
    Refund applyRefund(Integer orderId, Integer userId, String reason, String description);
    
    /**
     * 申请退款（带图片）
     * @param orderId 订单ID
     * @param userId 用户ID
     * @param reason 退款原因
     * @param description 详细描述
     * @param images 退款凭证图片，多张图片用逗号分隔
     * @return 退款记录
     */
    Refund applyRefund(Integer orderId, Integer userId, String reason, String description, String images);
    
    /**
     * 获取退款详情
     * @param id 退款ID
     * @param userId 用户ID (用于权限验证)
     * @return 退款记录
     */
    Refund getRefundById(Integer id, Integer userId);
    
    /**
     * 获取用户退款列表
     * @param userId 用户ID
     * @param status 退款状态 (可选)
     * @return 退款记录列表
     */
    List<Refund> getRefundsByUserId(Integer userId, Integer status);
    
    /**
     * 获取订单退款记录
     * @param orderId 订单ID
     * @param userId 用户ID (用于权限验证)
     * @return 退款记录列表
     */
    List<Refund> getRefundsByOrderId(Integer orderId, Integer userId);
    
    /**
     * 管理员获取所有退款记录
     * @param status 退款状态 (可选)
     * @return 退款记录列表
     */
    List<Refund> getAllRefunds(Integer status);
    
    /**
     * 管理员处理退款
     * @param id 退款ID
     * @param status 处理结果 (1-通过 2-拒绝)
     * @param adminRemark 管理员备注
     * @return 是否成功
     */
    boolean processRefund(Integer id, Integer status, String adminRemark);
} 