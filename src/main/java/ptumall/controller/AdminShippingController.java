package ptumall.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ptumall.config.JWTInterceptors;
import ptumall.model.ShippingInfo;
import ptumall.service.OrderService;
import ptumall.service.ShippingService;
import ptumall.vo.Result;
import ptumall.vo.ShippingRouteVO;

import javax.servlet.http.HttpServletRequest;

/**
 * 管理员物流控制器
 * 处理商家发货相关的HTTP请求
 */
@Api(tags = "管理员物流接口")
@RestController
@RequestMapping("/api/admin/shipping")
public class AdminShippingController {

    @Autowired
    private ShippingService shippingService;
    
    @Autowired
    private OrderService orderService;

    /**
     * 创建物流信息（商家发货时调用）
     */
    @ApiOperation("创建物流信息")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "orderId", value = "订单ID", required = true, paramType = "query"),
        @ApiImplicitParam(name = "shippingCompany", value = "物流公司", required = true, paramType = "query"),
        @ApiImplicitParam(name = "trackingNumber", value = "物流单号", required = true, paramType = "query"),
        @ApiImplicitParam(name = "senderAddress", value = "发货地址", required = true, paramType = "query")
    })
    @PostMapping("/create")
    public Result<ShippingInfo> createShippingInfo(
            HttpServletRequest request,
            @RequestParam Integer orderId,
            @RequestParam String shippingCompany,
            @RequestParam String trackingNumber,
            @RequestParam String senderAddress) {
        // 检查用户权限（管理员）
        Integer userId = (Integer) request.getAttribute(JWTInterceptors.USER_ID_KEY);
        if (userId == null) {
            return Result.unauthorized();
        }
        
        // 创建物流信息
        ShippingInfo info = shippingService.createShippingInfo(
            orderId, shippingCompany, trackingNumber, senderAddress);
        
        // 更新订单状态为已发货
        orderService.shipOrder(orderId);
        
        return Result.success(info);
    }
    
    /**
     * 获取物流路线（管理员专用，可查看任何订单的物流）
     */
    @ApiOperation("获取物流路线（管理员专用）")
    @ApiImplicitParam(name = "orderId", value = "订单ID", required = true, paramType = "path")
    @GetMapping("/route/{orderId}")
    public Result<ShippingRouteVO> getShippingRoute(
            HttpServletRequest request,
            @PathVariable Integer orderId) {
        // 检查用户权限（管理员）
        Integer userId = (Integer) request.getAttribute(JWTInterceptors.USER_ID_KEY);
        if (userId == null) {
            return Result.unauthorized();
        }
        
        // 管理员可以查看任何订单的物流信息，无需验证订单所有权
        ShippingRouteVO route = shippingService.getShippingRoute(orderId, null);
        return Result.success(route);
    }
} 