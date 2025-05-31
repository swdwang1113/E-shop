package ptumall.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.util.UriComponentsBuilder;
import java.net.URI;

/**
 * 高德地图服务类
 */
@Service
public class GaodeMapService {
    
    private static final Logger logger = LoggerFactory.getLogger(GaodeMapService.class);
    
    @Value("${gaode.map.key}")
    private String apiKey;
    
    private final RestTemplate restTemplate = new RestTemplate();
    
    /**
     * 地址转换为经纬度
     * @param address 地址
     * @return 包含经纬度的Map
     */
    public Map<String, Object> geocode(String address) {
        if (address == null || address.trim().isEmpty()) {
            logger.error("地址为空");
            throw new IllegalArgumentException("地址不能为空");
        }
        
        try {
            logger.info("调用高德地图地理编码API: address={}", address);
            
            // 使用UriComponentsBuilder构建URL，自动处理URL编码
            URI uri = UriComponentsBuilder.fromHttpUrl("https://restapi.amap.com/v3/geocode/geo")
                .queryParam("address", address)
                .queryParam("key", apiKey)
                .build()
                .encode()
                .toUri();
            
            logger.debug("请求URL: {}", uri);
            
            Map<String, Object> response = restTemplate.getForObject(uri, Map.class);
            
            if (response == null) {
                logger.error("高德地图API返回为空");
                throw new RuntimeException("高德地图API返回为空");
            }
            
            logger.info("高德地图API响应: status={}, info={}", response.get("status"), response.get("info"));
            
            if (!"1".equals(response.get("status"))) {
                logger.error("高德地图API错误: {}", response.get("info"));
                throw new RuntimeException("高德地图API错误: " + response.get("info"));
            }
            
            return response;
        } catch (Exception e) {
            logger.error("调用高德地图地理编码API失败", e);
            throw new RuntimeException("调用高德地图地理编码API失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 获取两点间路线规划
     * @param startLng 起点经度
     * @param startLat 起点纬度
     * @param endLng 终点经度
     * @param endLat 终点纬度
     * @return 路线规划结果
     */
    public Map<String, Object> getRoute(String startLng, String startLat, String endLng, String endLat) {
        if (startLng == null || startLat == null || endLng == null || endLat == null) {
            logger.error("坐标参数不完整");
            throw new IllegalArgumentException("坐标参数不能为空");
        }
        
        try {
            String origin = startLng + "," + startLat;
            String destination = endLng + "," + endLat;
            
            logger.info("调用高德地图路径规划API: origin={}, destination={}", origin, destination);
            
            // 使用UriComponentsBuilder构建URL，自动处理URL编码
            URI uri = UriComponentsBuilder.fromHttpUrl("https://restapi.amap.com/v3/direction/driving")
                .queryParam("origin", origin)
                .queryParam("destination", destination)
                .queryParam("key", apiKey)
                .build()
                .encode()
                .toUri();
            
            logger.debug("请求URL: {}", uri);
            
            Map<String, Object> response = restTemplate.getForObject(uri, Map.class);
            
            if (response == null) {
                logger.error("高德地图API返回为空");
                throw new RuntimeException("高德地图API返回为空");
            }
            
            logger.info("高德地图API响应: status={}, info={}", response.get("status"), response.get("info"));
            
            if (!"1".equals(response.get("status"))) {
                logger.error("高德地图API错误: {}", response.get("info"));
                // 不抛出异常，而是返回错误信息，让调用者处理
            }
            
            return response;
        } catch (Exception e) {
            logger.error("调用高德地图路径规划API失败", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "0");
            errorResponse.put("info", "调用高德地图路径规划API失败: " + e.getMessage());
            return errorResponse;
        }
    }
    
    /**
     * 从高德地图API响应中提取经纬度
     * @param geoResponse 高德地图API响应
     * @return 经纬度字符串，格式为"经度,纬度"
     */
    public String extractLocation(Map<String, Object> geoResponse) {
        if (geoResponse == null) {
            logger.error("地理编码响应为空");
            throw new IllegalArgumentException("地理编码响应不能为空");
        }
        
        try {
            if (!"1".equals(geoResponse.get("status"))) {
                logger.error("地理编码API返回错误: {}", geoResponse.get("info"));
                throw new RuntimeException("地理编码API返回错误: " + geoResponse.get("info"));
            }
            
            List<Map<String, Object>> geocodes = (List<Map<String, Object>>) geoResponse.get("geocodes");
            
            if (geocodes == null || geocodes.isEmpty()) {
                logger.error("未找到地理编码结果");
                throw new RuntimeException("未找到地理编码结果");
            }
            
            String location = (String) geocodes.get(0).get("location");
            
            if (location == null || location.trim().isEmpty()) {
                logger.error("地理编码结果中没有location字段");
                throw new RuntimeException("地理编码结果中没有location字段");
            }
            
            logger.info("提取到的位置: {}", location);
            return location;
        } catch (ClassCastException e) {
            logger.error("解析地理编码响应失败", e);
            logger.error("地理编码响应内容: {}", geoResponse);
            throw new RuntimeException("解析地理编码响应失败: " + e.getMessage(), e);
        } catch (Exception e) {
            logger.error("提取位置信息失败", e);
            throw new RuntimeException("提取位置信息失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 估算配送时间
     * @param distance 距离（米）
     * @param speed 速度（km/h）
     * @return 预计配送时间（分钟）
     */
    public int estimateDeliveryTime(double distance, double speed) {
        if (distance < 0) {
            logger.warn("距离为负值: {}, 使用绝对值", distance);
            distance = Math.abs(distance);
        }
        
        if (speed <= 0) {
            logger.warn("速度小于等于0: {}, 使用默认值40km/h", speed);
            speed = 40.0;
        }
        
        // 将距离转换为公里
        double distanceInKm = distance / 1000;
        // 计算时间（小时）
        double timeInHours = distanceInKm / speed;
        // 转换为分钟并添加30分钟的处理时间
        int timeInMinutes = (int) (timeInHours * 60) + 30;
        
        logger.info("估算配送时间: 距离={}米, 速度={}km/h, 时间={}分钟", distance, speed, timeInMinutes);
        return timeInMinutes;
    }
} 