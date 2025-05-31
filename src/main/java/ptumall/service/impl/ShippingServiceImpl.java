package ptumall.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ptumall.dao.OrderDao;
import ptumall.dao.ShippingInfoDao;
import ptumall.dao.UserAddressDao;
import ptumall.model.Orders;
import ptumall.model.ShippingInfo;
import ptumall.model.UserAddress;
import ptumall.service.GaodeMapService;
import ptumall.service.ShippingService;
import ptumall.vo.ShippingRouteVO;

import java.math.BigDecimal;
import java.util.*;
import java.util.Date;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 物流服务实现类
 */
@Service
public class ShippingServiceImpl implements ShippingService {
    
    private static final Logger logger = LoggerFactory.getLogger(ShippingServiceImpl.class);
    
    @Autowired
    private GaodeMapService gaodeMapService;
    
    @Autowired
    private ShippingInfoDao shippingInfoDao;
    
    @Autowired
    private OrderDao orderDao;
    
    @Autowired
    private UserAddressDao userAddressDao;
    
    /**
     * 创建物流信息（商家发货时调用）
     * 
     * @param orderId 订单ID
     * @param shippingCompany 物流公司
     * @param trackingNumber 物流单号
     * @param senderAddress 发货地址
     * @return 创建的物流信息
     */
    @Override
    @Transactional
    public ShippingInfo createShippingInfo(Integer orderId, String shippingCompany, 
                                         String trackingNumber, String senderAddress) {
        logger.info("开始创建物流信息: orderId={}, company={}, tracking={}, address={}",
                   orderId, shippingCompany, trackingNumber, senderAddress);
                   
        // 1. 获取订单信息
        Orders order = orderDao.selectById(orderId);
        if (order == null) {
            logger.error("订单不存在: orderId={}", orderId);
            throw new RuntimeException("订单不存在");
        }
        
        // 2. 获取收货地址
        UserAddress address = userAddressDao.selectById(order.getAddressId());
        if (address == null) {
            logger.error("收货地址不存在: addressId={}", order.getAddressId());
            throw new RuntimeException("收货地址不存在");
        }
        
        String receiverAddress = address.getProvince() + address.getCity() + 
                               address.getDistrict() + address.getAddress();
        logger.info("收货地址: {}", receiverAddress);
        
        // 3. 地址转换为经纬度
        Map<String, Object> senderGeo = null;
        Map<String, Object> receiverGeo = null;
        try {
            logger.info("开始解析发货地址: {}", senderAddress);
            senderGeo = gaodeMapService.geocode(senderAddress);
            logger.info("发货地址解析结果: {}", senderGeo);
            
            logger.info("开始解析收货地址: {}", receiverAddress);
            receiverGeo = gaodeMapService.geocode(receiverAddress);
            logger.info("收货地址解析结果: {}", receiverGeo);
        } catch (Exception e) {
            logger.error("地址解析失败", e);
            throw new RuntimeException("地址解析失败: " + e.getMessage());
        }
        
        // 4. 提取经纬度
        String senderLocation = null;
        String receiverLocation = null;
        try {
            senderLocation = gaodeMapService.extractLocation(senderGeo);
            receiverLocation = gaodeMapService.extractLocation(receiverGeo);
            logger.info("发货地址坐标: {}", senderLocation);
            logger.info("收货地址坐标: {}", receiverLocation);
        } catch (Exception e) {
            logger.error("提取经纬度失败", e);
            throw new RuntimeException("提取经纬度失败: " + e.getMessage());
        }
        
        String[] senderCoords = senderLocation.split(",");
        String[] receiverCoords = receiverLocation.split(",");
        
        // 5. 创建物流信息
        ShippingInfo shippingInfo = new ShippingInfo();
        shippingInfo.setOrderId(orderId);
        shippingInfo.setShippingCompany(shippingCompany);
        shippingInfo.setTrackingNumber(trackingNumber);
        shippingInfo.setSenderAddress(senderAddress);
        shippingInfo.setSenderLongitude(new BigDecimal(senderCoords[0]));
        shippingInfo.setSenderLatitude(new BigDecimal(senderCoords[1]));
        shippingInfo.setReceiverAddress(receiverAddress);
        shippingInfo.setReceiverLongitude(new BigDecimal(receiverCoords[0]));
        shippingInfo.setReceiverLatitude(new BigDecimal(receiverCoords[1]));
        
        // 6. 计算预计送达时间（假设平均速度为40km/h）
        try {
            logger.info("开始计算路线规划");
            Map<String, Object> routeInfo = gaodeMapService.getRoute(
                senderCoords[0], senderCoords[1], receiverCoords[0], receiverCoords[1]);
            logger.info("路线规划结果: {}", routeInfo);
            
            if (routeInfo != null && "1".equals(routeInfo.get("status"))) {
                try {
                    // 正确处理路径规划API返回的数据结构
                    Map<String, Object> routeData = (Map<String, Object>) routeInfo.get("route");
                    if (routeData != null) {
                        List<Map<String, Object>> paths = (List<Map<String, Object>>) routeData.get("paths");
                        if (paths != null && !paths.isEmpty()) {
                            Map<String, Object> path = paths.get(0);
                            String distanceStr = String.valueOf(path.get("distance"));
                            double distance = Double.parseDouble(distanceStr);
                            int deliveryTimeMinutes = gaodeMapService.estimateDeliveryTime(distance, 40);
                            logger.info("预计配送时间: {}分钟, 距离: {}米", deliveryTimeMinutes, distance);
                            
                            // 设置预计送达时间
                            Calendar calendar = Calendar.getInstance();
                            calendar.add(Calendar.MINUTE, deliveryTimeMinutes);
                            shippingInfo.setEstimatedTime(calendar.getTime());
                        } else {
                            // 如果没有找到路径，设置默认的预计送达时间
                            logger.warn("未找到路径，使用默认送达时间");
                            setDefaultEstimatedTime(shippingInfo);
                        }
                    } else {
                        // 如果route为空，设置默认的预计送达时间
                        logger.warn("路线数据为空，使用默认送达时间");
                        setDefaultEstimatedTime(shippingInfo);
                    }
                } catch (Exception e) {
                    // 发生异常，记录错误并设置默认的预计送达时间
                    logger.error("解析路径数据时出错", e);
                    setDefaultEstimatedTime(shippingInfo);
                }
            } else {
                // 如果路由计算失败，设置默认的预计送达时间
                logger.warn("路线规划失败，使用默认送达时间");
                setDefaultEstimatedTime(shippingInfo);
            }
        } catch (Exception e) {
            logger.error("计算路线时出错", e);
            setDefaultEstimatedTime(shippingInfo);
        }
        
        // 7. 保存物流信息
        try {
            logger.info("保存物流信息");
            
            // 设置创建时间和更新时间
            Date now = new Date();
            shippingInfo.setCreateTime(now);
            shippingInfo.setUpdateTime(now);
            
            shippingInfoDao.insert(shippingInfo);
            logger.info("物流信息创建成功: id={}", shippingInfo.getId());
        } catch (Exception e) {
            logger.error("保存物流信息失败", e);
            throw new RuntimeException("保存物流信息失败: " + e.getMessage());
        }
        
        return shippingInfo;
    }
    
    /**
     * 设置默认的预计送达时间（3天后）
     */
    private void setDefaultEstimatedTime(ShippingInfo shippingInfo) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, 3); // 默认3天后送达
        shippingInfo.setEstimatedTime(calendar.getTime());
        logger.info("设置默认送达时间: {}", shippingInfo.getEstimatedTime());
    }
    
    /**
     * 获取物流路线（包含模拟路线数据）
     * 
     * @param orderId 订单ID
     * @param userId 用户ID（用于权限验证）
     * @return 物流路线信息
     */
    @Override
    public ShippingRouteVO getShippingRoute(Integer orderId, Integer userId) {
        logger.info("获取物流路线: orderId={}, userId={}", orderId, userId);
        
        // 1. 获取物流信息
        ShippingInfo shippingInfo = shippingInfoDao.selectByOrderId(orderId);
        if (shippingInfo == null) {
            logger.error("物流信息不存在: orderId={}", orderId);
            throw new RuntimeException("物流信息不存在");
        }
        
        // 检查并处理可能为null的时间字段
        if (shippingInfo.getCreateTime() == null) {
            logger.warn("物流信息的创建时间为null，设置为当前时间");
            shippingInfo.setCreateTime(new Date());
        }
        
        if (shippingInfo.getUpdateTime() == null) {
            logger.warn("物流信息的更新时间为null，设置为当前时间");
            shippingInfo.setUpdateTime(new Date());
        }
        
        // 2. 获取订单信息（验证权限）
        Orders order = orderDao.selectById(orderId);
        if (order == null) {
            logger.error("订单不存在: orderId={}", orderId);
            throw new RuntimeException("订单不存在");
        }
        
        // 验证用户是否有权限查看该订单的物流信息
        // 当userId为null时，表示是管理员调用，无需验证权限
        if (userId != null && !order.getUserId().equals(userId)) {
            logger.error("无权限查看该订单的物流信息: orderId={}, userId={}, orderUserId={}", 
                        orderId, userId, order.getUserId());
            throw new RuntimeException("无权限查看该订单的物流信息");
        }
        
        // 3. 获取路线规划
        Map<String, Object> route = null;
        try {
            logger.info("获取路线规划");
            route = gaodeMapService.getRoute(
                shippingInfo.getSenderLongitude().toString(),
                shippingInfo.getSenderLatitude().toString(),
                shippingInfo.getReceiverLongitude().toString(),
                shippingInfo.getReceiverLatitude().toString()
            );
            logger.info("路线规划结果获取成功");
        } catch (Exception e) {
            logger.error("获取路线规划失败", e);
            route = new HashMap<>();
            route.put("status", "0");
            route.put("info", "获取路线规划失败: " + e.getMessage());
        }
        
        // 4. 生成模拟的物流路径点（根据起点和终点之间的路线生成几个中间点）
        List<Map<String, Object>> pathPoints = generateSimulatedPathPoints(
            shippingInfo.getSenderLongitude().doubleValue(),
            shippingInfo.getSenderLatitude().doubleValue(),
            shippingInfo.getReceiverLongitude().doubleValue(),
            shippingInfo.getReceiverLatitude().doubleValue()
        );
        
        // 5. 组装返回数据
        ShippingRouteVO routeVO = new ShippingRouteVO();
        routeVO.setShippingInfo(shippingInfo);
        
        // 设置发货地址和收货地址坐标
        Map<String, Double> senderLocation = new HashMap<>();
        senderLocation.put("longitude", shippingInfo.getSenderLongitude().doubleValue());
        senderLocation.put("latitude", shippingInfo.getSenderLatitude().doubleValue());
        routeVO.setSenderLocation(senderLocation);
        
        Map<String, Double> receiverLocation = new HashMap<>();
        receiverLocation.put("longitude", shippingInfo.getReceiverLongitude().doubleValue());
        receiverLocation.put("latitude", shippingInfo.getReceiverLatitude().doubleValue());
        routeVO.setReceiverLocation(receiverLocation);
        
        routeVO.setRouteInfo(route);
        routeVO.setPathPoints(pathPoints);
        
        logger.info("物流路线获取成功");
        return routeVO;
    }
    
    /**
     * 生成模拟的物流路径点
     * 根据起点和终点之间的路线生成几个中间点
     * 
     * @param startLng 起点经度
     * @param startLat 起点纬度
     * @param endLng 终点经度
     * @param endLat 终点纬度
     * @return 路径点列表
     */
    private List<Map<String, Object>> generateSimulatedPathPoints(
            double startLng, double startLat, double endLng, double endLat) {
        logger.info("生成模拟路径点: 从({},{})到({},{})", startLng, startLat, endLng, endLat);
        List<Map<String, Object>> pathPoints = new ArrayList<>();
        
        // 添加起点
        Map<String, Object> startPoint = new HashMap<>();
        startPoint.put("longitude", startLng);
        startPoint.put("latitude", startLat);
        startPoint.put("status", 1); // 已发货
        startPoint.put("time", new Date(System.currentTimeMillis() - 3 * 24 * 60 * 60 * 1000)); // 3天前
        pathPoints.add(startPoint);
        
        // 生成3-5个中间点
        int pointCount = 3 + new Random().nextInt(3); // 3到5个点
        for (int i = 1; i <= pointCount; i++) {
            double ratio = (double) i / (pointCount + 1);
            double lng = startLng + (endLng - startLng) * ratio;
            double lat = startLat + (endLat - startLat) * ratio;
            
            // 添加一些随机偏移，使路径看起来更自然
            lng += (Math.random() - 0.5) * 0.05;
            lat += (Math.random() - 0.5) * 0.05;
            
            Map<String, Object> point = new HashMap<>();
            point.put("longitude", lng);
            point.put("latitude", lat);
            point.put("status", 2); // 运输中
            
            // 时间递增
            long timeOffset = (long) (ratio * 3 * 24 * 60 * 60 * 1000); // 最多3天
            point.put("time", new Date(System.currentTimeMillis() - (3 * 24 * 60 * 60 * 1000 - timeOffset)));
            
            pathPoints.add(point);
        }
        
        // 如果包裹已送达，添加终点
        Map<String, Object> endPoint = new HashMap<>();
        endPoint.put("longitude", endLng);
        endPoint.put("latitude", endLat);
        endPoint.put("status", 3); // 已送达
        endPoint.put("time", new Date()); // 当前时间
        pathPoints.add(endPoint);
        
        logger.info("生成了{}个路径点", pathPoints.size());
        return pathPoints;
    }
} 