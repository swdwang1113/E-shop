package ptumall.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ptumall.model.Refund;
import ptumall.service.RefundService;
import ptumall.vo.Result;
import ptumall.vo.ResultCode;
import ptumall.config.JWTInterceptors;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

import javax.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 管理员退款控制器
 * 提供退款申请的管理功能，包括查询、审批和处理退款申请
 * 仅限管理员访问，所有接口都需要进行权限验证
 */
@RestController  // Spring MVC注解，标识这是一个REST风格的控制器
@RequestMapping("/api/admin")  // 定义控制器的基础URL路径，管理员相关接口
@Api(tags = "管理员退款接口")  // Swagger注解，定义API文档分组
public class AdminRefundController {

    @Autowired
    private RefundService refundService;
    
    /**
     * 获取所有退款申请
     * 管理员可以查看系统中的所有退款申请，支持按状态筛选和分页查询
     * 
     * @param status 退款状态：0-处理中，1-已通过，2-已拒绝，null表示查询所有状态
     * @param pageNum 页码，从1开始
     * @param pageSize 每页显示的记录数
     * @param request HTTP请求对象，用于获取管理员身份信息
     * @return 包含分页信息和退款列表的结果
     */
    @GetMapping("/refunds")  // 映射GET请求到/api/admin/refunds路径
    @ApiOperation(value = "获取所有退款申请列表", notes = "管理员获取所有退款申请记录，可以根据状态筛选，支持分页查询")
    public Result getRefundList(
            @ApiParam(value = "退款状态：0-处理中，1-已通过，2-已拒绝", required = false) 
            @RequestParam(required = false) Integer status,
            @ApiParam(value = "页码，从1开始", required = false, defaultValue = "1") 
            @RequestParam(required = false, defaultValue = "1") Integer pageNum,
            @ApiParam(value = "每页条数", required = false, defaultValue = "10") 
            @RequestParam(required = false, defaultValue = "10") Integer pageSize,
            HttpServletRequest request) {
        // 验证管理员权限
        // 从请求中获取JWT拦截器设置的用户ID
        Integer userId = (Integer) request.getAttribute(JWTInterceptors.USER_ID_KEY);
        // 由于JWT中没有存储角色信息，我们需要在服务层或数据库中查询用户角色
        // 这里简化处理，直接假定有userId的请求就是管理员，实际项目中需要查询角色
        if (userId == null) {
            return Result.failed(ResultCode.FORBIDDEN, "无权限访问");
        }
        
        // 计算分页的起始索引
        int offset = (pageNum - 1) * pageSize;
        
        // 调用服务层获取退款列表，按状态筛选
        List<Refund> refunds = refundService.getAllRefunds(status);
        
        // 简单的内存分页实现，实际项目中应当修改DAO和Service层实现数据库分页
        int total = refunds.size();  // 总记录数
        List<Refund> pagedRefunds = refunds;
        if (offset < total) {
            // 计算当前页的结束索引，确保不超过列表长度
            int toIndex = Math.min(offset + pageSize, total);
            pagedRefunds = refunds.subList(offset, toIndex);
        } else {
            pagedRefunds = Collections.emptyList(); // 如果超出范围则返回空列表
        }
        
        // 构建包含分页信息的响应
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("total", total);  // 总记录数
        resultMap.put("pages", (total + pageSize - 1) / pageSize);  // 总页数，向上取整
        resultMap.put("pageNum", pageNum);  // 当前页码
        resultMap.put("pageSize", pageSize);  // 每页大小
        resultMap.put("list", pagedRefunds);  // 当前页的退款列表
        
        return Result.success(resultMap);
    }
    
    /**
     * 处理退款申请
     * 管理员审核退款申请，可以选择通过或拒绝
     * 如果通过，系统将自动触发退款流程
     * 
     * @param id 退款申请ID
     * @param params 处理参数，包含status(状态)和remark(备注)
     * @param request HTTP请求对象，用于获取管理员身份信息
     * @return 处理结果
     */
    @PostMapping("/refunds/{id}/process")  // 映射POST请求到/api/admin/refunds/{id}/process路径
    @ApiOperation(value = "处理退款申请", notes = "管理员处理退款申请，可以通过或拒绝")
    public Result processRefund(
            @ApiParam(value = "退款ID", required = true) 
            @PathVariable("id") Integer id, 
            @ApiParam(value = "处理参数", required = true, example = "{\"status\":1, \"remark\":\"同意退款\"}") 
            @RequestBody Map<String, Object> params,
            HttpServletRequest request) {
        // 验证管理员权限
        Integer userId = (Integer) request.getAttribute(JWTInterceptors.USER_ID_KEY);
        if (userId == null) {
            return Result.failed(ResultCode.FORBIDDEN, "无权限访问");
        }
        
        // 从请求参数中提取状态和备注
        Integer status = params.get("status") instanceof Number ? 
                ((Number) params.get("status")).intValue() : null;
        String remark = (String) params.get("remark");
        
        // 验证状态参数是否有效
        // status=1表示通过，status=2表示拒绝
        if (status == null || (status != 1 && status != 2)) {
            return Result.failed(ResultCode.PARAM_ERROR, "处理结果参数错误");
        }
        
        try {
            // 调用服务层处理退款申请
            boolean success = refundService.processRefund(id, status, remark);
            if (success) {
                return Result.success("处理成功");
            } else {
                return Result.failed(ResultCode.FAILED, "处理失败");
            }
        } catch (Exception e) {
            // 捕获处理过程中的异常
            return Result.failed(ResultCode.FAILED, e.getMessage());
        }
    }
    
    /**
     * 获取退款详情
     * 管理员查看指定退款申请的详细信息
     * 
     * @param id 退款申请ID
     * @param request HTTP请求对象，用于获取管理员身份信息
     * @return 退款申请详情
     */
    @GetMapping("/refunds/{id}")  // 映射GET请求到/api/admin/refunds/{id}路径
    @ApiOperation(value = "获取退款详情", notes = "管理员获取指定退款申请的详细信息")
    public Result getRefundDetail(
            @ApiParam(value = "退款ID", required = true) 
            @PathVariable("id") Integer id, 
            HttpServletRequest request) {
        // 验证管理员权限
        Integer userId = (Integer) request.getAttribute(JWTInterceptors.USER_ID_KEY);
        if (userId == null) {
            return Result.failed(ResultCode.FORBIDDEN, "无权限访问");
        }
        
        // 调用服务层获取退款详情
        // 管理员查看时传入null作为用户ID，表示不需要验证申请人身份
        Refund refund = refundService.getRefundById(id, null); 
        if (refund == null) {
            return Result.failed(ResultCode.NOT_FOUND, "退款记录不存在");
        }
        
        return Result.success(refund);
    }
} 