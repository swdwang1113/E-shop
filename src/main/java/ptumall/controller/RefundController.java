package ptumall.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ptumall.entity.Refund;
import ptumall.service.FileService;
import ptumall.service.RefundService;
import ptumall.vo.Result;
import ptumall.vo.ResultCode;
import ptumall.config.JWTInterceptors;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 退款控制器
 */
@RestController
@RequestMapping("/api")
@Api(tags = "用户退款接口")
public class RefundController {
    
    private static final Logger logger = LoggerFactory.getLogger(RefundController.class);
    
    @Autowired
    private RefundService refundService;
    
    @Autowired
    private FileService fileService;
    
    @Value("${image.prefix-url}")
    private String imageUrlPrefix;
    
    /**
     * 获取退款详情
     */
    @GetMapping("/refunds/{id}")
    @ApiOperation(value = "获取退款详情", notes = "用户获取自己的退款申请详情")
    public Result getRefundDetail(
            @ApiParam(value = "退款ID", required = true) 
            @PathVariable("id") Integer id, 
            HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute(JWTInterceptors.USER_ID_KEY);
        if (userId == null) {
            return Result.failed(ResultCode.UNAUTHORIZED, "请先登录");
        }
        
        Refund refund = refundService.getRefundById(id, userId);
        if (refund == null) {
            return Result.failed(ResultCode.NOT_FOUND, "退款记录不存在或无权查看");
        }
        
        return Result.success(refund);
    }
    
    /**
     * 获取用户退款列表
     */
    @GetMapping("/refunds")
    @ApiOperation(value = "获取用户退款列表", notes = "获取当前登录用户的所有退款申请记录，可以根据状态筛选，支持分页查询")
    public Result getRefundList(
            @ApiParam(value = "退款状态：0-处理中，1-已通过，2-已拒绝", required = false) 
            @RequestParam(required = false) Integer status,
            @ApiParam(value = "页码，从1开始", required = false, defaultValue = "1") 
            @RequestParam(required = false, defaultValue = "1") Integer pageNum,
            @ApiParam(value = "每页条数", required = false, defaultValue = "10") 
            @RequestParam(required = false, defaultValue = "10") Integer pageSize,
            HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute(JWTInterceptors.USER_ID_KEY);
        if (userId == null) {
            return Result.failed(ResultCode.UNAUTHORIZED, "请先登录");
        }
        
        // 计算起始索引
        int offset = (pageNum - 1) * pageSize;
        
        List<Refund> refunds = refundService.getRefundsByUserId(userId, status);
        
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
     * 上传退款凭证图片
     */
    @PostMapping("/refunds/upload/image")
    @ApiOperation(value = "上传退款凭证图片", notes = "上传退款凭证图片，返回图片URL")
    public Result uploadRefundImage(
            @ApiParam(value = "图片文件", required = true) 
            @RequestParam("file") MultipartFile file,
            HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute(JWTInterceptors.USER_ID_KEY);
        if (userId == null) {
            return Result.failed(ResultCode.UNAUTHORIZED, "请先登录");
        }
        
        if (file.isEmpty()) {
            return Result.failed(ResultCode.PARAM_ERROR, "上传文件不能为空");
        }
        
        try {
            // 检查文件类型
            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null || !(originalFilename.endsWith(".jpg") || 
                    originalFilename.endsWith(".jpeg") || originalFilename.endsWith(".png"))) {
                return Result.failed(ResultCode.PARAM_ERROR, "只支持jpg、jpeg、png格式的图片");
            }
            
            // 使用FileService上传图片到OSS
            String imageUrl = fileService.uploadImage(file, "refund");
            if (imageUrl == null) {
                return Result.failed(ResultCode.FAILED, "图片上传失败");
            }
            
            logger.info("用户{}上传退款凭证图片成功: {}", userId, imageUrl);
            
            return Result.success(imageUrl);
        } catch (Exception e) {
            logger.error("上传退款凭证图片失败: ", e);
            return Result.failed(ResultCode.FAILED, "上传图片失败: " + e.getMessage());
        }
    }
    
    /**
     * 申请退款
     */
    @PostMapping("/orders/{id}/refund")
    @ApiOperation(value = "申请退款", notes = "用户申请订单退款，需要提供退款原因和凭证图片")
    public Result applyRefund(
            @ApiParam(value = "订单ID", required = true) 
            @PathVariable("id") Integer orderId, 
            @ApiParam(value = "退款参数", required = true, example = "{\"reason\":\"商品质量问题\", \"description\":\"商品收到后发现有破损\", \"images\":\"http://example.com/img1.jpg,http://example.com/img2.jpg\"}") 
            @RequestBody Map<String, String> params,
            HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute(JWTInterceptors.USER_ID_KEY);
        if (userId == null) {
            return Result.failed(ResultCode.UNAUTHORIZED, "请先登录");
        }
        
        String reason = params.get("reason");
        String description = params.get("description");
        String images = params.get("images"); // 图片URL，多个用逗号分隔
        
        if (reason == null || reason.trim().isEmpty()) {
            return Result.failed(ResultCode.PARAM_ERROR, "退款原因不能为空");
        }
        
        try {
            Refund refund = refundService.applyRefund(orderId, userId, reason, description, images);
            return Result.success("退款申请已提交", refund);
        } catch (Exception e) {
            return Result.failed(ResultCode.FAILED, e.getMessage());
        }
    }
} 