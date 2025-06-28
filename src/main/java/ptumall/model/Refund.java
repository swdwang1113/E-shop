package ptumall.model;

import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 退款实体类
 */
@Data
public class Refund implements Serializable {
    
    /**
     * ID
     */
    private Integer id;
    
    /**
     * 订单ID
     */
    private Integer orderId;
    
    /**
     * 用户ID
     */
    private Integer userId;
    
    /**
     * 退款金额
     */
    private BigDecimal refundAmount;
    
    /**
     * 退款原因
     */
    private String reason;
    
    /**
     * 详细描述
     */
    private String description;
    
    /**
     * 退款凭证图片，多张图片用逗号分隔
     */
    private String images;
    
    /**
     * 退款状态：0-处理中，1-已通过，2-已拒绝
     */
    private Integer status;
    
    /**
     * 管理员备注
     */
    private String adminRemark;
    
    /**
     * 创建时间
     */
    private Date createTime;
    
    /**
     * 更新时间
     */
    private Date updateTime;
} 