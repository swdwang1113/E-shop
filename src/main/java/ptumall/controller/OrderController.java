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
import ptumall.vo.CreateOrderParam;
import ptumall.vo.PageResult;
import ptumall.vo.Result;

import javax.servlet.http.HttpServletRequest;

@Api(tags = "订单接口")
@RestController
@RequestMapping("/api/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @ApiOperation("创建订单")
    @PostMapping("")
    public Result<Orders> createOrder(HttpServletRequest request, @RequestBody CreateOrderParam param) {
        Integer userId = (Integer) request.getAttribute(JWTInterceptors.USER_ID_KEY);
        if (userId == null) {
            return Result.unauthorized();
        }
        Orders order = orderService.createOrder(userId, param);
        return Result.success(order);
    }

    @ApiOperation("获取订单详情")
    @ApiImplicitParam(name = "id", value = "订单ID", required = true, paramType = "path")
    @GetMapping("/{id}")
    public Result<Orders> getOrderDetail(HttpServletRequest request, @PathVariable("id") Integer id) {
        Integer userId = (Integer) request.getAttribute(JWTInterceptors.USER_ID_KEY);
        if (userId == null) {
            return Result.unauthorized();
        }
        Orders order = orderService.getOrderDetail(userId, id);
        return Result.success(order);
    }

    @ApiOperation("根据订单编号获取订单详情")
    @ApiImplicitParam(name = "orderNo", value = "订单编号", required = true, paramType = "query")
    @GetMapping("/no")
    public Result<Orders> getOrderByOrderNo(HttpServletRequest request, @RequestParam("orderNo") String orderNo) {
        Integer userId = (Integer) request.getAttribute(JWTInterceptors.USER_ID_KEY);
        if (userId == null) {
            return Result.unauthorized();
        }
        Orders order = orderService.getOrderByOrderNo(userId, orderNo);
        return Result.success(order);
    }

    @ApiOperation("获取订单列表")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "pageNum", value = "页码", required = true, paramType = "query"),
        @ApiImplicitParam(name = "pageSize", value = "每页数量", required = true, paramType = "query")
    })
    @GetMapping("")
    public Result<PageResult<Orders>> getOrderList(
        HttpServletRequest request,
        @RequestParam(defaultValue = "1") Integer pageNum,
        @RequestParam(defaultValue = "10") Integer pageSize
    ) {
        Integer userId = (Integer) request.getAttribute(JWTInterceptors.USER_ID_KEY);
        if (userId == null) {
            return Result.unauthorized();
        }
        PageResult<Orders> result = orderService.getOrderList(userId, pageNum, pageSize);
        return Result.success(result);
    }

    @ApiOperation("取消订单")
    @ApiImplicitParam(name = "id", value = "订单ID", required = true, paramType = "path")
    @PostMapping("/{id}/cancel")
    public Result<Boolean> cancelOrder(HttpServletRequest request, @PathVariable("id") Integer id) {
        Integer userId = (Integer) request.getAttribute(JWTInterceptors.USER_ID_KEY);
        if (userId == null) {
            return Result.unauthorized();
        }
        boolean success = orderService.cancelOrder(userId, id);
        return Result.success(success);
    }

    @ApiOperation("支付订单")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "id", value = "订单ID", required = true, paramType = "path"),
        @ApiImplicitParam(name = "paymentType", value = "支付方式", required = true, paramType = "query", example = "1")
    })
    @PostMapping("/{id}/pay")
    public Result<Boolean> payOrder(
        HttpServletRequest request,
        @PathVariable("id") Integer id,
        @RequestParam("paymentType") Byte paymentType
    ) {
        Integer userId = (Integer) request.getAttribute(JWTInterceptors.USER_ID_KEY);
        if (userId == null) {
            return Result.unauthorized();
        }
        boolean success = orderService.payOrder(userId, id, paymentType);
        return Result.success(success);
    }

    @ApiOperation("确认收货")
    @ApiImplicitParam(name = "id", value = "订单ID", required = true, paramType = "path")
    @PostMapping("/{id}/receipt")
    public Result<Boolean> confirmReceipt(HttpServletRequest request, @PathVariable("id") Integer id) {
        Integer userId = (Integer) request.getAttribute(JWTInterceptors.USER_ID_KEY);
        if (userId == null) {
            return Result.unauthorized();
        }
        boolean success = orderService.confirmReceipt(userId, id);
        return Result.success(success);
    }

    @ApiOperation("删除订单")
    @ApiImplicitParam(name = "id", value = "订单ID", required = true, paramType = "path")
    @DeleteMapping("/{id}")
    public Result<Boolean> deleteOrder(HttpServletRequest request, @PathVariable("id") Integer id) {
        Integer userId = (Integer) request.getAttribute(JWTInterceptors.USER_ID_KEY);
        if (userId == null) {
            return Result.unauthorized();
        }
        boolean success = orderService.deleteUserOrder(userId, id);
        return Result.success(success);
    }
}
