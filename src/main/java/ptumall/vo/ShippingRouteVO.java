package ptumall.vo;

import lombok.Data;
import ptumall.model.ShippingInfo;

import java.util.List;
import java.util.Map;

/**
 * 物流路线视图对象
 * 用于前端展示物流路线信息
 */
@Data
public class ShippingRouteVO {
    // 物流基本信息
    private ShippingInfo shippingInfo;
    
    // 发货地址和收货地址坐标
    private Map<String, Double> senderLocation;
    private Map<String, Double> receiverLocation;
    
    // 路线信息（包含路径点、距离等）
    private Map<String, Object> routeInfo;
    
    // 模拟的物流路径点（用于地图展示）
    private List<Map<String, Object>> pathPoints;
} 