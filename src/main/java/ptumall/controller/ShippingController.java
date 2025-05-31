package ptumall.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ptumall.config.JWTInterceptors;
import ptumall.service.ShippingService;
import ptumall.vo.Result;
import ptumall.vo.ShippingRouteVO;

import javax.servlet.http.HttpServletRequest;

/**
 * 物流控制器
 * 处理用户查看物流路线的HTTP请求
 */
@Api(tags = "物流接口")
@RestController
@RequestMapping("/api/shipping")
public class ShippingController {

    @Autowired
    private ShippingService shippingService;

    /**
     * 获取物流路线
     */
    @ApiOperation("获取物流路线")
    @ApiImplicitParam(name = "orderId", value = "订单ID", required = true, paramType = "path")
    @GetMapping("/route/{orderId}")
    public Result<ShippingRouteVO> getShippingRoute(
            HttpServletRequest request,
            @PathVariable Integer orderId) {
        // 检查用户权限
        Integer userId = (Integer) request.getAttribute(JWTInterceptors.USER_ID_KEY);
        if (userId == null) {
            return Result.unauthorized();
        }
        
        ShippingRouteVO route = shippingService.getShippingRoute(orderId, userId);
        return Result.success(route);
    }
}