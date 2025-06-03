package ptumall.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ptumall.config.JWTInterceptors;
import ptumall.model.Orders;
import ptumall.service.AlipayService;
import ptumall.service.OrderService;
import ptumall.vo.CreateOrderParam;
import ptumall.vo.PageResult;
import ptumall.vo.Result;
import ptumall.vo.ResultCode;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * 订单控制器
 * 处理所有与订单相关的HTTP请求
 * 包括：创建订单、查询订单、取消订单、支付订单等操作
 */
@Api(tags = "订单接口")
@RestController
@RequestMapping("/api/orders")
public class OrderController {

    /**
     * 订单服务接口
     * 用于处理订单相关的业务逻辑
     */
    @Autowired
    private OrderService orderService;

    /**
     * 支付宝服务接口
     * 用于处理支付宝支付相关的业务逻辑
     */
    @Autowired
    private AlipayService alipayService;

    /**
     * 创建新订单
     * 接收订单创建参数，创建订单并返回订单信息
     * 
     * @param request HTTP请求对象，用于获取当前用户ID
     * @param param 订单创建参数，包含订单项信息
     * @return 创建成功的订单信息
     */
    @ApiOperation("创建订单")
    @PostMapping("")
    public Result<Orders> createOrder(HttpServletRequest request, @RequestBody CreateOrderParam param) {
        // 从请求中获取用户ID
        Integer userId = (Integer) request.getAttribute(JWTInterceptors.USER_ID_KEY);
        if (userId == null) {
            return Result.unauthorized();  // 用户未登录
        }
        // 调用服务创建订单
        Orders order = orderService.createOrder(userId, param);
        return Result.success(order);
    }

    /**
     * 获取订单详情
     * 根据订单ID查询订单详细信息
     * 
     * @param request HTTP请求对象
     * @param id 订单ID
     * @return 订单详细信息
     */
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

    /**
     * 根据订单编号获取订单详情
     * 通过订单编号查询订单信息
     * 
     * @param request HTTP请求对象
     * @param orderNo 订单编号
     * @return 订单详细信息
     */
    @ApiOperation("根据订单编号获取订单详情")
    @ApiImplicitParam(name = "orderNo", value = "订单编号", required = true, paramType = "query")
    @GetMapping("/no")
    public Result<Orders> getOrderByOrderNo(HttpServletRequest request, @RequestParam("orderNo") String orderNo) {
        Integer userId = (Integer) request.getAttribute(JWTInterceptors.USER_ID_KEY);
        if (userId == null) {
            return Result.unauthorized();
        }
        
        // 处理可能包含用户ID的订单号
        String originalOrderNo = orderNo;
        if (orderNo.contains("_")) {
            originalOrderNo = orderNo.split("_")[0];
        }
        
        Orders order = orderService.getOrderByOrderNo(userId, originalOrderNo);
        return Result.success(order);
    }

    /**
     * 获取订单列表
     * 支持分页查询和按状态筛选
     * 
     * @param request HTTP请求对象
     * @param pageNum 页码，默认1
     * @param pageSize 每页数量，默认10
     * @param status 订单状态（可选）：0-待付款 1-已付款 2-已发货 3-已完成 4-已取消
     * @return 分页的订单列表
     */
    @ApiOperation("获取订单列表")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "pageNum", value = "页码", required = true, paramType = "query"),
        @ApiImplicitParam(name = "pageSize", value = "每页数量", required = true, paramType = "query"),
        @ApiImplicitParam(name = "status", value = "订单状态(可选): 0-待付款 1-已付款 2-已发货 3-已完成 4-已取消", required = false, paramType = "query")
    })
    @GetMapping("")
    public Result<PageResult<Orders>> getOrderList(
        HttpServletRequest request,
        @RequestParam(defaultValue = "1") Integer pageNum,
        @RequestParam(defaultValue = "10") Integer pageSize,
        @RequestParam(required = false) Byte status
    ) {
        Integer userId = (Integer) request.getAttribute(JWTInterceptors.USER_ID_KEY);
        if (userId == null) {
            return Result.unauthorized();
        }
        
        PageResult<Orders> result;
        if (status == null) {
            // 不传状态参数，查询全部订单
            result = orderService.getOrderList(userId, pageNum, pageSize);
        } else {
            // 根据状态查询订单
            result = orderService.getOrderListByStatus(userId, status, pageNum, pageSize);
        }
        
        return Result.success(result);
    }

    /**
     * 取消订单
     * 将订单状态更新为已取消
     * 
     * @param request HTTP请求对象
     * @param id 订单ID
     * @return 取消操作是否成功
     */
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

    /**
     * 支付订单
     * 处理订单支付操作
     * 
     * @param request HTTP请求对象
     * @param response HTTP响应对象
     * @param id 订单ID
     * @param paymentType 支付方式
     * @return 支付操作是否成功
     */
    @ApiOperation("支付订单")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "id", value = "订单ID", required = true, paramType = "path"),
        @ApiImplicitParam(name = "paymentType", value = "支付方式(1支付宝，2微信，3银行卡)", required = true, paramType = "query", example = "1")
    })
    @PostMapping("/{id}/pay")
    public Result<Object> payOrder(
        HttpServletRequest request,
        HttpServletResponse response,
        @PathVariable("id") Integer id,
        @RequestParam("paymentType") Byte paymentType
    ) throws IOException {
        Integer userId = (Integer) request.getAttribute(JWTInterceptors.USER_ID_KEY);
        if (userId == null) {
            return Result.unauthorized();
        }
        
        // 如果是支付宝支付
        if (paymentType == 1) {
            // 获取订单信息
            Orders order = orderService.getOrderDetail(userId, id);
            if (order == null) {
                return Result.failure(ResultCode.NOT_FOUND, "订单不存在");
            }
            
            // 创建支付宝支付表单
            String orderNo = order.getOrderNo() + "_" + userId; // 添加用户ID到订单号中，方便回调时识别
            String payForm = alipayService.createPayForm(id.longValue(), orderNo, order.getTotalAmount().doubleValue(), "订单支付");
            
            if (payForm != null) {
                // 直接输出HTML表单
                response.setContentType("text/html;charset=UTF-8");
                PrintWriter out = response.getWriter();
                out.write(payForm);
                out.flush();
                out.close();
                return null;
            } else {
                return Result.failure(ResultCode.FAILED, "生成支付表单失败");
            }
        } else {
            // 其他支付方式走原有流程
            boolean success = orderService.payOrder(userId, id, paymentType);
            return Result.success(success);
        }
    }

    /**
     * 确认收货
     * 将订单状态更新为已完成
     * 
     * @param request HTTP请求对象
     * @param id 订单ID
     * @return 确认收货操作是否成功
     */
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

    /**
     * 删除订单
     * 软删除订单记录
     * 
     * @param request HTTP请求对象
     * @param id 订单ID
     * @return 删除操作是否成功
     */
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
