package ptumall.service;

import ptumall.model.ShippingInfo;
import ptumall.vo.ShippingRouteVO;

/**
 * 物流服务接口
 */
public interface ShippingService {
    
    /**
     * 创建物流信息（商家发货时调用）
     * 
     * @param orderId 订单ID
     * @param shippingCompany 物流公司
     * @param trackingNumber 物流单号
     * @param senderAddress 发货地址
     * @return 创建的物流信息
     */
    ShippingInfo createShippingInfo(Integer orderId, String shippingCompany, 
                                    String trackingNumber, String senderAddress);
    
    /**
     * 获取物流路线（包含模拟路线数据）
     * 
     * @param orderId 订单ID
     * @param userId 用户ID（用于权限验证）
     * @return 物流路线信息
     */
    ShippingRouteVO getShippingRoute(Integer orderId, Integer userId);
} 