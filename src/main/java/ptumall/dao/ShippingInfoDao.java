package ptumall.dao;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import ptumall.model.ShippingInfo;

@Mapper
public interface ShippingInfoDao {
    /**
     * 创建物流信息
     * @param shippingInfo 物流信息
     * @return 影响行数
     */
    int insert(ShippingInfo shippingInfo);
    
    /**
     * 根据订单ID查询物流信息
     * @param orderId 订单ID
     * @return 物流信息
     */
    ShippingInfo selectByOrderId(Integer orderId);
    
    /**
     * 更新物流信息
     * @param shippingInfo 物流信息
     * @return 影响行数
     */
    int update(ShippingInfo shippingInfo);
    
    /**
     * 删除物流信息
     * @param id 物流信息ID
     * @return 影响行数
     */
    int deleteById(Integer id);
    
    /**
     * 根据订单ID删除物流信息
     * @param orderId 订单ID
     * @return 影响行数
     */
    int deleteByOrderId(Integer orderId);
} 