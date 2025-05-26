package ptumall.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 订单统计数据VO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderStatisticsVO {
    
    /**
     * 订单总数
     */
    private Integer totalOrders;
    
    /**
     * 销售总额
     */
    private BigDecimal totalSales;
} 