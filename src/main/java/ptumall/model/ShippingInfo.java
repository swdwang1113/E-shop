package ptumall.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import java.math.BigDecimal;
import java.util.Date;

@Data
@ApiModel("物流信息实体类")
public class ShippingInfo {
    @ApiModelProperty(value = "物流信息ID", example = "1")
    private Integer id;
    
    @ApiModelProperty(value = "订单ID", example = "1")
    private Integer orderId;
    
    @ApiModelProperty(value = "物流公司", example = "顺丰速运")
    private String shippingCompany;
    
    @ApiModelProperty(value = "物流单号", example = "SF1234567890")
    private String trackingNumber;
    
    @ApiModelProperty(value = "发货地址", example = "广东省深圳市南山区科技园1号")
    private String senderAddress;
    
    @ApiModelProperty(value = "发货地址经度", example = "114.057868")
    private BigDecimal senderLongitude;
    
    @ApiModelProperty(value = "发货地址纬度", example = "22.543099")
    private BigDecimal senderLatitude;
    
    @ApiModelProperty(value = "收货地址", example = "广东省广州市天河区天河路385号")
    private String receiverAddress;
    
    @ApiModelProperty(value = "收货地址经度", example = "113.330607")
    private BigDecimal receiverLongitude;
    
    @ApiModelProperty(value = "收货地址纬度", example = "23.137900")
    private BigDecimal receiverLatitude;
    
    @ApiModelProperty(value = "预计送达时间")
    private Date estimatedTime;
    
    @ApiModelProperty(value = "创建时间", hidden = true)
    private Date createTime;
    
    @ApiModelProperty(value = "更新时间", hidden = true)
    private Date updateTime;
} 