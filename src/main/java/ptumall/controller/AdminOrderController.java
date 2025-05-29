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

/**
 * 管理员订单控制器
 * 提供管理员对订单进行管理的相关接口
 * 包括：查询订单列表、订单详情、订单发货、删除订单等功能
 */
@Api(tags = "管理员订单接口")
@RestController
@RequestMapping("/api/admin/orders")
public class AdminOrderController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private AuthUtils authUtils;

    /**
     * 获取所有订单列表
     * 支持多种查询条件组合：
     * 1. 按订单状态查询
     * 2. 按订单号关键词查询
     * 3. 按用户ID查询
     * 4. 以上条件的组合查询
     * 
     * @param request HTTP请求对象
     * @param pageNum 页码，默认1
     * @param pageSize 每页数量，默认10
     * @param status 订单状态(可选): 0-待付款 1-已付款 2-已发货 3-已完成 4-已取消
     * @param orderNo 订单号关键词(可选)
     * @param userId 用户ID(可选)
     * @return 分页的订单列表数据
     */
    @ApiOperation("获取所有订单列表")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "pageNum", value = "页码", required = true, paramType = "query"),
        @ApiImplicitParam(name = "pageSize", value = "每页数量", required = true, paramType = "query"),
        @ApiImplicitParam(name = "status", value = "订单状态(可选): 0-待付款 1-已付款 2-已发货 3-已完成 4-已取消", required = false, paramType = "query"),
        @ApiImplicitParam(name = "orderNo", value = "订单号关键词(可选)", required = false, paramType = "query"),
        @ApiImplicitParam(name = "userId", value = "用户ID(可选)", required = false, paramType = "query")
    })
    @GetMapping("")
    public Result<PageResult<Orders>> getAllOrders(
        HttpServletRequest request,
        @RequestParam(defaultValue = "1") Integer pageNum,
        @RequestParam(defaultValue = "10") Integer pageSize,
        @RequestParam(required = false) Byte status,
        @RequestParam(required = false) String orderNo,
        @RequestParam(required = false) Integer userId
    ) {
        // 权限校验：只有管理员可以查看所有订单
        if (!authUtils.isAdmin(request)) {
            return Result.unauthorized();
        }
        
        PageResult<Orders> result;
        
        // 根据不同的参数组合调用不同的查询方法
        if (orderNo != null && !orderNo.trim().isEmpty()) {
            // 优先按订单号查询
            if (status != null) {
                // 订单号 + 状态
                result = orderService.searchOrdersByOrderNoAndStatus(orderNo, status, pageNum, pageSize);
            } else {
                // 仅订单号
                result = orderService.searchOrdersByOrderNo(orderNo, pageNum, pageSize);
            }
        } else if (userId != null) {
            // 按用户ID查询
            if (status != null) {
                // 用户ID + 状态
                result = orderService.searchOrdersByUserIdAndStatus(userId, status, pageNum, pageSize);
            } else {
                // 仅用户ID
                result = orderService.searchOrdersByUserId(userId, pageNum, pageSize);
            }
        } else if (status != null) {
            // 仅按状态查询
            result = orderService.getAllOrderListByStatus(status, pageNum, pageSize);
        } else {
            // 查询所有订单
            result = orderService.getAllOrderList(pageNum, pageSize);
        }
        
        return Result.success(result);
    }
    
    /**
     * 获取订单详情
     * 管理员可以查看任意订单的详细信息
     * 
     * @param request HTTP请求对象
     * @param id 订单ID
     * @return 订单详细信息
     */
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

    /**
     * 订单发货
     * 管理员可以将已支付的订单标记为已发货状态
     * 
     * @param request HTTP请求对象
     * @param id 订单ID
     * @return 发货操作是否成功
     */
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
    
    /**
     * 删除订单
     * 管理员可以删除任意订单
     * 注意：删除订单会同时删除订单商品信息
     * 
     * @param request HTTP请求对象
     * @param id 订单ID
     * @return 删除操作是否成功
     */
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
    
    /**
     * 获取订单统计数据
     * 包括总订单数和总销售额
     * 
     * @param request HTTP请求对象
     * @return 订单统计信息
     */
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