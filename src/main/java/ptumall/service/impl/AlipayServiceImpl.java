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
 * 支付宝支付服务实现
 */
@Slf4j
@Service
public class AlipayServiceImpl implements AlipayService {

    @Autowired
    private AlipayClient alipayClient;
    
    @Autowired
    private AlipayConfig alipayConfig;
    
    @Autowired
    private OrderService orderService;
    
    @Override
    public String createPayForm(Long orderId, String orderNo, Double amount, String subject) {
        // 创建API对应的request
        AlipayTradePagePayRequest alipayRequest = new AlipayTradePagePayRequest();
        
        // 设置回调地址
        alipayRequest.setReturnUrl(alipayConfig.getReturnUrl());
        alipayRequest.setNotifyUrl(alipayConfig.getNotifyUrl());
        
        // 组装业务参数
        try {
            // 构建业务参数
            String bizContent = "{" +
                    "\"out_trade_no\":\"" + orderNo + "\"," +
                    "\"product_code\":\"FAST_INSTANT_TRADE_PAY\"," +
                    "\"total_amount\":" + amount + "," +
                    "\"subject\":\"" + subject + "\"," +
                    "\"body\":\"" + subject + "\"" +
                    "}";
            
            alipayRequest.setBizContent(bizContent);
            
            // 调用SDK生成表单
            String form = alipayClient.pageExecute(alipayRequest).getBody();
            log.info("生成支付宝支付表单成功，订单号：{}", orderNo);
            return form;
            
        } catch (AlipayApiException e) {
            log.error("生成支付宝支付表单失败", e);
            return null;
        }
    }
    
    @Override
    public boolean handleNotify(Map<String, String> params) {
        log.info("接收到支付宝异步通知：{}", params);
        
        try {
            // 验证签名
            boolean signVerified = AlipaySignature.rsaCheckV1(
                    params, 
                    alipayConfig.getPublicKey(), 
                    alipayConfig.getCharset(), 
                    alipayConfig.getSignType());
            
            if (!signVerified) {
                log.error("支付宝异步通知验签失败");
                return false;
            }
            
            // 验证通知内容
            String tradeStatus = params.get("trade_status");
            String outTradeNo = params.get("out_trade_no");
            
            // 交易成功或交易完成
            if ("TRADE_SUCCESS".equals(tradeStatus) || "TRADE_FINISHED".equals(tradeStatus)) {
                try {
                    log.info("支付宝异步通知，订单号：{}, 交易状态：{}", outTradeNo, tradeStatus);
                    
                    // 从订单号中提取原始订单号和用户ID
                    String[] parts = outTradeNo.split("_");
                    if (parts.length < 2) {
                        log.error("订单号格式不正确，无法提取订单号和用户ID: {}", outTradeNo);
                        return false;
                    }
                    
                    String orderNoStr = parts[0];
                    String userIdStr = parts[1];
                    
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
                    log.error("处理支付宝通知时发生异常", e);
                    return false;
                }
            }
            
            return false;
            
        } catch (AlipayApiException e) {
            log.error("处理支付宝异步通知失败", e);
            return false;
        }
    }
    
    @Override
    public boolean queryPayStatus(String orderNo) {
        try {
            AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();
            
            // 设置业务参数
            String bizContent = "{" +
                    "\"out_trade_no\":\"" + orderNo + "\"" +
                    "}";
            request.setBizContent(bizContent);
            
            // 执行查询
            AlipayTradeQueryResponse response = alipayClient.execute(request);
            
            if (response.isSuccess()) {
                String tradeStatus = response.getTradeStatus();
                // 交易成功或交易完成
                return "TRADE_SUCCESS".equals(tradeStatus) || "TRADE_FINISHED".equals(tradeStatus);
            } else {
                log.error("查询支付宝订单状态失败：{}, {}", response.getCode(), response.getMsg());
                return false;
            }
            
        } catch (AlipayApiException e) {
            log.error("查询支付宝订单状态异常", e);
            return false;
        }
    }

    @Override
    public boolean refund(String orderNo, Double refundAmount, String refundReason) {
        log.info("申请支付宝退款：订单号={}, 退款金额={}, 退款原因={}", orderNo, refundAmount, refundReason);
        
        // 最大重试次数
        int maxRetries = 3;
        // 重试间隔（毫秒）
        long retryInterval = 2000;
        
        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                AlipayTradeRefundRequest request = new AlipayTradeRefundRequest();
                
                // 设置业务参数
                String bizContent = "{" +
                        "\"out_trade_no\":\"" + orderNo + "\"," +
                        "\"refund_amount\":" + refundAmount + "," +
                        "\"refund_reason\":\"" + refundReason + "\"" +
                        "}";
                request.setBizContent(bizContent);
                
                // 执行退款
                AlipayTradeRefundResponse response = alipayClient.execute(request);
                
                if (response.isSuccess()) {
                    log.info("支付宝退款成功：订单号={}, 退款金额={}", orderNo, refundAmount);
                    return true;
                } else {
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
                        Thread.sleep(retryInterval);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        log.error("重试等待被中断", e);
                    }
                }
            } catch (AlipayApiException e) {
                log.error("支付宝退款异常：订单号=" + orderNo, e);
                
                // 如果是最后一次尝试，则返回失败
                if (attempt == maxRetries) {
                    log.error("支付宝退款重试{}次后仍然发生异常，放弃重试：订单号={}", maxRetries, orderNo);
                    return false;
                }
                
                // 等待一段时间后重试
                log.info("支付宝退款异常，准备第{}次重试：订单号={}", attempt + 1, orderNo);
                try {
                    Thread.sleep(retryInterval);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    log.error("重试等待被中断", ie);
                }
            }
        }
        
        return false;
    }
} 