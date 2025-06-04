package ptumall.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ptumall.dao.OrderDao;
import ptumall.dao.RefundDao;
import ptumall.dao.GoodsDao;
import ptumall.entity.Refund;
import ptumall.model.Orders;
import ptumall.model.OrderItems;
import ptumall.service.RefundService;
import ptumall.service.AlipayService;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * 退款服务实现类
 */
@Service
@Transactional
public class RefundServiceImpl implements RefundService {
    
    private static final Logger logger = LoggerFactory.getLogger(RefundServiceImpl.class);
    
    @Autowired
    private RefundDao refundDao;
    
    @Autowired
    private OrderDao orderDao;
    
    @Autowired
    private GoodsDao goodsDao;
    
    @Autowired
    private AlipayService alipayService;
    
    @Override
    public Refund applyRefund(Integer orderId, Integer userId, String reason, String description, String images) {
        logger.info("申请退款(带图片): orderId={}, userId={}, reason={}, images={}", orderId, userId, reason, images);
        
        // 1. 获取订单信息
        Orders order = orderDao.selectById(orderId);
        if (order == null) {
            logger.error("订单不存在: orderId={}", orderId);
            throw new RuntimeException("订单不存在");
        }
        
        // 2. 验证订单是否属于当前用户
        if (!order.getUserId().equals(userId)) {
            logger.error("订单不属于当前用户: orderId={}, userId={}, orderUserId={}", 
                    orderId, userId, order.getUserId());
            throw new RuntimeException("无权操作此订单");
        }
        
        // 3. 验证订单状态是否允许退款（只有已支付状态的订单才能申请退款）
        if (order.getStatus() != 1 && order.getStatus() != 2) {
            logger.error("订单状态不允许退款: orderId={}, status={}", orderId, order.getStatus());
            throw new RuntimeException("订单状态不允许退款，只有已支付的订单才能申请退款");
        }
        
        // 4. 检查是否已经申请过退款
        List<Refund> existingRefunds = refundDao.findByOrderId(orderId);
        for (Refund existingRefund : existingRefunds) {
            if (existingRefund.getStatus() == 0) {
                logger.error("订单已经有正在处理的退款申请: orderId={}, refundId={}", 
                        orderId, existingRefund.getId());
                throw new RuntimeException("订单已经有正在处理的退款申请");
            }
        }
        
        // 5. 创建退款记录
        Refund refund = new Refund();
        refund.setOrderId(orderId);
        refund.setUserId(userId);
        refund.setRefundAmount(order.getTotalAmount()); // 退款金额为支付金额
        refund.setReason(reason);
        refund.setDescription(description);
        refund.setImages(images); // 设置退款凭证图片
        refund.setStatus(0); // 0-处理中
        
        Date now = new Date();
        refund.setCreateTime(now);
        refund.setUpdateTime(now);
        
        // 6. 保存退款记录
        refundDao.insert(refund);
        logger.info("退款申请已创建: refundId={}", refund.getId());
        
        return refund;
    }
    
    @Override
    public Refund applyRefund(Integer orderId, Integer userId, String reason, String description) {
        // 调用带图片的方法，图片参数传null
        return applyRefund(orderId, userId, reason, description, null);
    }
    
    @Override
    public Refund getRefundById(Integer id, Integer userId) {
        Refund refund = refundDao.findById(id);
        if (refund == null) {
            return null;
        }
        
        // 如果不是管理员，验证是否属于当前用户
        if (userId != null && !refund.getUserId().equals(userId)) {
            logger.error("退款记录不属于当前用户: refundId={}, userId={}, refundUserId={}", 
                    id, userId, refund.getUserId());
            return null;
        }
        
        return refund;
    }
    
    @Override
    public List<Refund> getRefundsByUserId(Integer userId, Integer status) {
        return refundDao.findByUserId(userId, status);
    }
    
    @Override
    public List<Refund> getRefundsByOrderId(Integer orderId, Integer userId) {
        // 验证订单是否属于当前用户
        Orders order = orderDao.selectById(orderId);
        if (order == null) {
            logger.error("订单不存在: orderId={}", orderId);
            throw new RuntimeException("订单不存在");
        }
        
        if (userId != null && !order.getUserId().equals(userId)) {
            logger.error("订单不属于当前用户: orderId={}, userId={}, orderUserId={}", 
                    orderId, userId, order.getUserId());
            throw new RuntimeException("无权查看此订单的退款记录");
        }
        
        return refundDao.findByOrderId(orderId);
    }
    
    @Override
    public List<Refund> getAllRefunds(Integer status) {
        return refundDao.findAll(status);
    }
    
    @Override
    @Transactional
    public boolean processRefund(Integer id, Integer status, String adminRemark) {
        logger.info("处理退款: refundId={}, status={}", id, status);
        
        // 1. 获取退款记录
        Refund refund = refundDao.findById(id);
        if (refund == null) {
            logger.error("退款记录不存在: refundId={}", id);
            throw new RuntimeException("退款记录不存在");
        }
        
        // 2. 验证退款状态是否为处理中
        if (refund.getStatus() != 0) {
            logger.error("退款记录已处理: refundId={}, status={}", id, refund.getStatus());
            throw new RuntimeException("退款记录已处理");
        }
        
        // 3. 更新退款状态
        refund.setStatus(status);
        refund.setAdminRemark(adminRemark);
        refund.setUpdateTime(new Date());
        
        refundDao.updateById(refund);
        
        // 4. 如果通过退款，则更新订单状态，并恢复商品库存
        if (status == 1) {
            Orders order = orderDao.selectById(refund.getOrderId());
            if (order == null) {
                logger.error("订单不存在: orderId={}", refund.getOrderId());
                throw new RuntimeException("订单不存在");
            }
            
            // 判断支付方式，如果是支付宝支付(paymentType=1)，则调用支付宝退款接口
            if (order.getPaymentType() != null && order.getPaymentType() == 1) {
                // 构建支付宝退款的订单号（原始订单号_用户ID）
                String alipayOrderNo = order.getOrderNo() + "_" + order.getUserId();
                Double refundAmount = refund.getRefundAmount().doubleValue();
                String refundReason = refund.getReason();
                
                // 调用支付宝退款
                boolean refundResult = alipayService.refund(alipayOrderNo, refundAmount, refundReason);
                if (!refundResult) {
                    logger.error("支付宝退款失败: orderId={}, orderNo={}", order.getId(), order.getOrderNo());
                    throw new RuntimeException("支付宝退款失败");
                }
                logger.info("支付宝退款成功: orderId={}, orderNo={}", order.getId(), order.getOrderNo());
            }
            
            // 4.1 更新订单状态为"已退款"
            order.setStatus((byte)5); // 5-已退款
            order.setUpdateTime(new Date());
            orderDao.updateById(order);
            
            // 4.2 恢复商品库存
            List<OrderItems> orderItems = orderDao.selectOrderItemsByOrderId(order.getId());
            for (OrderItems item : orderItems) {
                goodsDao.updateStock(item.getGoodsId(), 
                        goodsDao.findById(item.getGoodsId()).getStock() + item.getQuantity());
                logger.info("恢复商品库存: goodsId={}, quantity={}", item.getGoodsId(), item.getQuantity());
            }
            
            logger.info("退款已通过，订单状态已更新为已退款: orderId={}", order.getId());
        } else {
            logger.info("退款已拒绝: refundId={}", id);
        }
        
        return true;
    }
} 