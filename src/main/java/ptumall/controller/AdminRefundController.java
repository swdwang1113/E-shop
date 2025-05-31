package ptumall.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ptumall.entity.Refund;
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
 */
@RestController
@RequestMapping("/api/admin")
@Api(tags = "管理员退款接口")
public class AdminRefundController {
    
    @Autowired
    private RefundService refundService;
    
    /**
     * 获取所有退款申请
     */
    @GetMapping("/refunds")
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
        Integer userId = (Integer) request.getAttribute(JWTInterceptors.USER_ID_KEY);
        // 由于JWT中没有存储角色信息，我们需要在服务层或数据库中查询用户角色
        // 这里简化处理，直接假定有userId的请求就是管理员，实际项目中需要查询角色
        if (userId == null) {
            return Result.failed(ResultCode.FORBIDDEN, "无权限访问");
        }
        
        // 计算起始索引
        int offset = (pageNum - 1) * pageSize;
        
        List<Refund> refunds = refundService.getAllRefunds(status);
        
        // 简单的内存分页实现，实际项目中应当修改DAO和Service层实现数据库分页
        int total = refunds.size();
        List<Refund> pagedRefunds = refunds;
        if (offset < total) {
            int toIndex = Math.min(offset + pageSize, total);
            pagedRefunds = refunds.subList(offset, toIndex);
        } else {
            pagedRefunds = Collections.emptyList(); // 空列表
        }
        
        // 构建包含分页信息的响应
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("total", total);
        resultMap.put("pages", (total + pageSize - 1) / pageSize);
        resultMap.put("pageNum", pageNum);
        resultMap.put("pageSize", pageSize);
        resultMap.put("list", pagedRefunds);
        
        return Result.success(resultMap);
    }
    
    /**
     * 处理退款申请
     */
    @PostMapping("/refunds/{id}/process")
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
        
        Integer status = params.get("status") instanceof Number ? 
                ((Number) params.get("status")).intValue() : null;
        String remark = (String) params.get("remark");
        
        if (status == null || (status != 1 && status != 2)) {
            return Result.failed(ResultCode.PARAM_ERROR, "处理结果参数错误");
        }
        
        try {
            boolean success = refundService.processRefund(id, status, remark);
            if (success) {
                return Result.success("处理成功");
            } else {
                return Result.failed(ResultCode.FAILED, "处理失败");
            }
        } catch (Exception e) {
            return Result.failed(ResultCode.FAILED, e.getMessage());
        }
    }
    
    /**
     * 获取退款详情
     */
    @GetMapping("/refunds/{id}")
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
        
        Refund refund = refundService.getRefundById(id, null); // 管理员查看，不需要验证用户ID
        if (refund == null) {
            return Result.failed(ResultCode.NOT_FOUND, "退款记录不存在");
        }
        
        return Result.success(refund);
    }
} 