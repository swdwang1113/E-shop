package ptumall.service.impl;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.request.AlipayTradeRefundRequest;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.alipay.api.response.AlipayTradeRefundResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ptumall.config.AlipayConfig;
import ptumall.service.AlipayService;
import ptumall.service.OrderService;
import ptumall.model.Orders;

import java.util.Map;

/**
 * 支付宝支付服务实现类
 * 提供支付宝支付相关的功能，包括：
 * 1. 创建支付表单
 * 2. 处理支付宝异步通知
 * 3. 查询支付状态
 * 4. 申请退款
 */
@Slf4j  // Lombok注解，自动创建日志对象log
@Service  // Spring服务注解，标识这是一个服务类
public class AlipayServiceImpl implements AlipayService {

    /**
     * 支付宝客户端，用于调用支付宝API
     * 由Spring自动注入，配置在AlipayConfig中
     */
    @Autowired
    private AlipayClient alipayClient;
    
    @Autowired
    private AlipayConfig alipayConfig;
    
    @Autowired
    private OrderService orderService;
    
    /**
     * 创建支付宝支付表单
     * 生成一个包含支付信息的HTML表单，前端可直接展示此表单引导用户跳转到支付宝付款页面
     *
     * @param orderId 订单ID
     * @param orderNo 订单编号
     * @param amount 支付金额
     * @param subject 订单标题/商品名称
     * @return 支付宝支付表单HTML字符串，如果生成失败则返回null
     */
    @Override
    public String createPayForm(Long orderId, String orderNo, Double amount, String subject) {
        // 创建API对应的request对象
        AlipayTradePagePayRequest alipayRequest = new AlipayTradePagePayRequest();
        
        // 设置支付完成后的回调地址和异步通知地址
        alipayRequest.setReturnUrl(alipayConfig.getReturnUrl());  // 支付成功后跳转页面
        alipayRequest.setNotifyUrl(alipayConfig.getNotifyUrl());  // 支付结果异步通知地址
        
        // 组装业务参数
        try {
            // 构建业务参数JSON字符串
            String bizContent = "{" +
                    "\"out_trade_no\":\"" + orderNo + "\"," +  // 商户订单号
                    "\"product_code\":\"FAST_INSTANT_TRADE_PAY\"," +  // 产品码，固定值
                    "\"total_amount\":" + amount + "," +  // 订单总金额
                    "\"subject\":\"" + subject + "\"," +  // 订单标题
                    "\"body\":\"" + subject + "\"" +  // 订单描述
                    "}";
            
            alipayRequest.setBizContent(bizContent);
            
            // 调用SDK生成表单
            String form = alipayClient.pageExecute(alipayRequest).getBody();
            log.info("生成支付宝支付表单成功，订单号：{}", orderNo);
            return form;
            
        } catch (AlipayApiException e) {
            // 捕获并记录异常
            log.error("生成支付宝支付表单失败", e);
            return null;
        }
    }
    
    /**
     * 处理支付宝异步通知
     * 验证通知的真实性并更新订单状态
     *
     * @param params 支付宝异步通知参数
     * @return 处理结果，true表示处理成功，false表示处理失败
     */
    @Override
    public boolean handleNotify(Map<String, String> params) {
        log.info("接收到支付宝异步通知：{}", params);
        
        try {
            // 验证签名，确保通知来自支付宝
            boolean signVerified = AlipaySignature.rsaCheckV1(
                    params, 
                    alipayConfig.getPublicKey(), 
                    alipayConfig.getCharset(), 
                    alipayConfig.getSignType());
            
            if (!signVerified) {
                // 签名验证失败，可能是伪造的通知
                log.error("支付宝异步通知验签失败");
                return false;
            }
            
            // 验证通知内容
            String tradeStatus = params.get("trade_status");  // 交易状态
            String outTradeNo = params.get("out_trade_no");   // 商户订单号
            
            // 只有交易成功或交易完成的通知才处理
            if ("TRADE_SUCCESS".equals(tradeStatus) || "TRADE_FINISHED".equals(tradeStatus)) {
                try {
                    log.info("支付宝异步通知，订单号：{}, 交易状态：{}", outTradeNo, tradeStatus);
                    
                    // 从订单号中提取原始订单号和用户ID
                    // 假设订单号格式为：原始订单号_用户ID
                    String[] parts = outTradeNo.split("_");
                    if (parts.length < 2) {
                        log.error("订单号格式不正确，无法提取订单号和用户ID: {}", outTradeNo);
                        return false;
                    }
                    
                    String orderNoStr = parts[0];  // 原始订单号
                    String userIdStr = parts[1];   // 用户ID
                    
                    // 将用户ID转换为整数
                    Integer userId;
                    try {
                        userId = Integer.parseInt(userIdStr);
                    } catch (NumberFormatException e) {
                        log.error("用户ID不是有效的整数: {}", userIdStr);
                        return false;
                    }
                    
                    // 根据订单号查询订单信息
                    Orders order = orderService.getOrderByOrderNo(userId, orderNoStr);
                    if (order == null) {
                        log.error("未找到订单: {}", orderNoStr);
                        return false;
                    }
                    
                    // 支付宝支付对应的支付方式为1
                    Byte paymentType = 1;
                    
                    // 使用订单ID和用户ID调用支付方法
                    return orderService.payOrder(userId, order.getId(), paymentType);
                } catch (Exception e) {
                    // 捕获处理过程中的所有异常
                    log.error("处理支付宝通知时发生异常", e);
                    return false;
                }
            }
            
            // 其他交易状态不处理
            return false;
            
        } catch (AlipayApiException e) {
            // 捕获支付宝API异常
            log.error("处理支付宝异步通知失败", e);
            return false;
        }
    }
    
    /**
     * 查询支付宝订单支付状态
     * 用于主动查询订单是否已支付
     *
     * @param orderNo 订单编号
     * @return 支付状态，true表示已支付，false表示未支付或查询失败
     */
    @Override
    public boolean queryPayStatus(String orderNo) {
        try {
            // 创建查询请求
            AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();
            
            // 设置业务参数
            String bizContent = "{" +
                    "\"out_trade_no\":\"" + orderNo + "\"" +  // 商户订单号
                    "}";
            request.setBizContent(bizContent);
            
            // 执行查询
            AlipayTradeQueryResponse response = alipayClient.execute(request);
            
            // 检查查询结果
            if (response.isSuccess()) {
                String tradeStatus = response.getTradeStatus();
                // 交易成功或交易完成表示已支付
                return "TRADE_SUCCESS".equals(tradeStatus) || "TRADE_FINISHED".equals(tradeStatus);
            } else {
                // 查询失败
                log.error("查询支付宝订单状态失败：{}, {}", response.getCode(), response.getMsg());
                return false;
            }
            
        } catch (AlipayApiException e) {
            // 捕获支付宝API异常
            log.error("查询支付宝订单状态异常", e);
            return false;
        }
    }

    /**
     * 申请支付宝退款
     * 支持自动重试机制，最多重试3次
     *
     * @param orderNo 订单编号
     * @param refundAmount 退款金额
     * @param refundReason 退款原因
     * @return 退款结果，true表示退款成功，false表示退款失败
     */
    @Override
    public boolean refund(String orderNo, Double refundAmount, String refundReason) {
        log.info("申请支付宝退款：订单号={}, 退款金额={}, 退款原因={}", orderNo, refundAmount, refundReason);
        
        // 最大重试次数
        int maxRetries = 3;
        // 重试间隔（毫秒）
        long retryInterval = 2000;  // 2秒
        
        // 重试循环
        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                // 创建退款请求
                AlipayTradeRefundRequest request = new AlipayTradeRefundRequest();
                
                // 设置业务参数
                String bizContent = "{" +
                        "\"out_trade_no\":\"" + orderNo + "\"," +  // 商户订单号
                        "\"refund_amount\":" + refundAmount + "," +  // 退款金额
                        "\"refund_reason\":\"" + refundReason + "\"" +  // 退款原因
                        "}";
                request.setBizContent(bizContent);
                
                // 执行退款
                AlipayTradeRefundResponse response = alipayClient.execute(request);
                
                // 检查退款结果
                if (response.isSuccess()) {
                    // 退款成功
                    log.info("支付宝退款成功：订单号={}, 退款金额={}", orderNo, refundAmount);
                    return true;
                } else {
                    // 退款失败
                    log.error("支付宝退款失败：订单号={}, 错误码={}, 错误信息={}, 子错误码={}, 子错误信息={}", 
                            orderNo, response.getCode(), response.getMsg(), 
                            response.getSubCode(), response.getSubMsg());
                    
                    // 如果是最后一次尝试，则返回失败
                    if (attempt == maxRetries) {
                        log.error("支付宝退款重试{}次后仍然失败，放弃重试：订单号={}", maxRetries, orderNo);
                        return false;
                    }
                    
                    // 等待一段时间后重试
                    log.info("支付宝退款失败，准备第{}次重试：订单号={}", attempt + 1, orderNo);
                    try {
                        Thread.sleep(retryInterval);  // 休眠一段时间
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();  // 重置中断标志
                        log.error("重试等待被中断", e);
                    }
                }
            } catch (AlipayApiException e) {
                // 捕获支付宝API异常
                log.error("支付宝退款异常：订单号=" + orderNo, e);
                
                // 如果是最后一次尝试，则返回失败
                if (attempt == maxRetries) {
                    log.error("支付宝退款重试{}次后仍然发生异常，放弃重试：订单号={}", maxRetries, orderNo);
                    return false;
                }
                
                // 等待一段时间后重试
                log.info("支付宝退款异常，准备第{}次重试：订单号={}", attempt + 1, orderNo);
                try {
                    Thread.sleep(retryInterval);  // 休眠一段时间
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();  // 重置中断标志
                    log.error("重试等待被中断", ie);
                }
            }
        }
        
        // 所有重试都失败
        return false;
    }
} 