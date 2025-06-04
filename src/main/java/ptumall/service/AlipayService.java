package ptumall.service;

/**
 * 支付宝支付服务接口
 */
public interface AlipayService {
    
    /**
     * 创建支付宝支付表单
     * 
     * @param orderId 订单ID
     * @param orderNo 订单编号
     * @param amount 支付金额
     * @param subject 订单标题
     * @return 支付表单HTML
     */
    String createPayForm(Long orderId, String orderNo, Double amount, String subject);
    
    /**
     * 处理支付宝异步通知
     * 
     * @param params 支付宝回调参数
     * @return 处理结果
     */
    boolean handleNotify(java.util.Map<String, String> params);
    
    /**
     * 查询支付状态
     * 
     * @param orderNo 订单编号
     * @return 是否支付成功
     */
    boolean queryPayStatus(String orderNo);
    
    /**
     * 支付宝退款
     * 
     * @param orderNo 订单编号
     * @param refundAmount 退款金额
     * @param refundReason 退款原因
     * @return 退款是否成功
     */
    boolean refund(String orderNo, Double refundAmount, String refundReason);
} 