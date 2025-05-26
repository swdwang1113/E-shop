package ptumall.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ptumall.config.JWTInterceptors;
import ptumall.model.Orders;
import ptumall.service.OrderService;
import ptumall.utils.AuthUtils;
import ptumall.vo.OrderStatisticsVO;
import ptumall.vo.PageResult;
import ptumall.vo.Result;

import javax.servlet.http.HttpServletRequest;

@Api(tags = "管理员订单接口")
@RestController
@RequestMapping("/api/admin/orders")
public class AdminOrderController {

    @Autowired
    private OrderService orderService;
    
    @Autowired
    private AuthUtils authUtils;

    @ApiOperation("获取所有订单列表")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "pageNum", value = "页码", required = true, paramType = "query"),
        @ApiImplicitParam(name = "pageSize", value = "每页数量", required = true, paramType = "query")
    })
    @GetMapping("")
    public Result<PageResult<Orders>> getAllOrders(
        HttpServletRequest request,
        @RequestParam(defaultValue = "1") Integer pageNum,
        @RequestParam(defaultValue = "10") Integer pageSize
    ) {
        // 权限校验：只有管理员可以查看所有订单
        if (!authUtils.isAdmin(request)) {
            return Result.unauthorized();
        }
        
        PageResult<Orders> result = orderService.getAllOrderList(pageNum, pageSize);
        return Result.success(result);
    }
    
    @ApiOperation("获取订单详情")
    @ApiImplicitParam(name = "id", value = "订单ID", required = true, paramType = "path")
    @GetMapping("/{id}")
    public Result<Orders> getOrderDetail(HttpServletRequest request, @PathVariable("id") Integer id) {
        // 权限校验：只有管理员可以查看任意订单详情
        if (!authUtils.isAdmin(request)) {
            return Result.unauthorized();
        }
        
        Orders order = orderService.getAdminOrderDetail(id);
        return Result.success(order);
    }

    @ApiOperation("订单发货")
    @ApiImplicitParam(name = "id", value = "订单ID", required = true, paramType = "path")
    @PostMapping("/{id}/ship")
    public Result<Boolean> shipOrder(HttpServletRequest request, @PathVariable("id") Integer id) {
        // 权限校验：只有管理员可以发货
        if (!authUtils.isAdmin(request)) {
            return Result.unauthorized();
        }
        
        boolean success = orderService.shipOrder(id);
        return Result.success(success);
    }
    
    @ApiOperation("删除订单")
    @ApiImplicitParam(name = "id", value = "订单ID", required = true, paramType = "path")
    @DeleteMapping("/{id}")
    public Result<Boolean> deleteOrder(HttpServletRequest request, @PathVariable("id") Integer id) {
        // 权限校验：只有管理员可以删除订单
        if (!authUtils.isAdmin(request)) {
            return Result.unauthorized();
        }
        
        boolean success = orderService.deleteOrder(id);
        return Result.success(success);
    }
    
    @ApiOperation("获取订单统计数据")
    @GetMapping("/statistics")
    public Result<OrderStatisticsVO> getOrderStatistics(HttpServletRequest request) {
        // 权限校验：只有管理员可以查看订单统计数据
        if (!authUtils.isAdmin(request)) {
            return Result.unauthorized();
        }
        
        OrderStatisticsVO statistics = orderService.getOrderStatistics();
        return Result.success(statistics);
    }
} 