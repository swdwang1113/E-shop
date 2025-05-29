package ptumall.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ptumall.config.JWTInterceptors;
import ptumall.model.GoodsReview;
import ptumall.service.FileService;
import ptumall.service.GoodsReviewService;
import ptumall.vo.Result;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 商品评价控制器
 * 提供商品评价相关的所有接口
 * 包括：添加评价、查询评价、点赞评价、上传评价图片等功能
 */
@RestController
@RequestMapping("/api/reviews")
@Api(tags = "商品评价接口")
public class GoodsReviewController {
    
    /**
     * 商品评价服务
     * 处理评价相关的业务逻辑
     */
    @Autowired
    private GoodsReviewService goodsReviewService;
    
    /**
     * 文件服务
     * 处理评价图片的上传
     */
    @Autowired
    private FileService fileService;
    
    /**
     * 添加商品评价
     * 用户可以对已购买的商品进行评价
     * 
     * @param request HTTP请求对象，用于获取当前用户ID
     * @param goodsReview 评价信息
     * @return 添加成功的评价信息
     */
    @PostMapping
    @ApiOperation("添加商品评价")
    public Result<GoodsReview> addReview(
            HttpServletRequest request,
            @RequestBody GoodsReview goodsReview) {
        Integer userId = (Integer) request.getAttribute(JWTInterceptors.USER_ID_KEY);
        goodsReview.setUserId(userId);
        return goodsReviewService.addReview(goodsReview);
    }
    
    /**
     * 获取商品评价列表
     * 查询指定商品的所有评价
     * 
     * @param goodsId 商品ID
     * @return 评价列表
     */
    @GetMapping("/goods/{goodsId}")
    @ApiOperation("获取商品评价列表")
    public Result<List<GoodsReview>> getReviewsByGoodsId(
            @ApiParam(value = "商品ID", required = true) @PathVariable Integer goodsId) {
        return goodsReviewService.getReviewsByGoodsId(goodsId);
    }
    
    /**
     * 获取用户的评价列表
     * 查询当前用户的所有评价
     * 
     * @param request HTTP请求对象，用于获取当前用户ID
     * @return 用户的评价列表
     */
    @GetMapping("/user")
    @ApiOperation("获取用户的评价列表")
    public Result<List<GoodsReview>> getReviewsByUserId(HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute(JWTInterceptors.USER_ID_KEY);
        return goodsReviewService.getReviewsByUserId(userId);
    }
    
    /**
     * 获取订单的评价列表
     * 查询指定订单的所有评价
     * 
     * @param request HTTP请求对象，用于获取当前用户ID
     * @param orderId 订单ID
     * @return 订单的评价列表
     */
    @GetMapping("/order/{orderId}")
    @ApiOperation("获取订单的评价列表")
    public Result<List<GoodsReview>> getReviewsByOrderId(
            HttpServletRequest request,
            @ApiParam(value = "订单ID", required = true) @PathVariable Integer orderId) {
        Integer userId = (Integer) request.getAttribute(JWTInterceptors.USER_ID_KEY);
        // 这里可以增加权限检查，确保用户只能查看自己的订单评价
        return goodsReviewService.getReviewsByOrderId(orderId);
    }
    
    /**
     * 检查是否已评价
     * 用于判断用户是否已经对指定商品进行过评价
     * 
     * @param request HTTP请求对象，用于获取当前用户ID
     * @param goodsId 商品ID
     * @param orderId 订单ID
     * @return 是否已评价
     */
    @GetMapping("/check")
    @ApiOperation("检查是否已评价")
    public Result<Boolean> checkReviewExists(
            HttpServletRequest request,
            @ApiParam(value = "商品ID", required = true) @RequestParam Integer goodsId,
            @ApiParam(value = "订单ID", required = true) @RequestParam Integer orderId) {
        Integer userId = (Integer) request.getAttribute(JWTInterceptors.USER_ID_KEY);
        return goodsReviewService.checkReviewExists(userId, goodsId, orderId);
    }
    
    /**
     * 点赞评价
     * 用户可以对评价进行点赞
     * 
     * @param request HTTP请求对象，用于获取当前用户ID
     * @param reviewId 评价ID
     * @return 操作结果
     */
    @PostMapping("/{reviewId}/like")
    @ApiOperation("点赞评价")
    public Result<Void> likeReview(
            HttpServletRequest request,
            @ApiParam(value = "评价ID", required = true) @PathVariable Integer reviewId) {
        Integer userId = (Integer) request.getAttribute(JWTInterceptors.USER_ID_KEY);
        return goodsReviewService.likeReview(reviewId, userId);
    }
    
    /**
     * 取消点赞评价
     * 用户可以取消对评价的点赞
     * 
     * @param request HTTP请求对象，用于获取当前用户ID
     * @param reviewId 评价ID
     * @return 操作结果
     */
    @DeleteMapping("/{reviewId}/like")
    @ApiOperation("取消点赞评价")
    public Result<Void> unlikeReview(
            HttpServletRequest request,
            @ApiParam(value = "评价ID", required = true) @PathVariable Integer reviewId) {
        Integer userId = (Integer) request.getAttribute(JWTInterceptors.USER_ID_KEY);
        return goodsReviewService.unlikeReview(reviewId, userId);
    }
    
    /**
     * 检查是否已点赞
     * 用于判断用户是否已经对指定评价进行过点赞
     * 
     * @param request HTTP请求对象，用于获取当前用户ID
     * @param reviewId 评价ID
     * @return 是否已点赞
     */
    @GetMapping("/{reviewId}/like/check")
    @ApiOperation("检查是否已点赞")
    public Result<Boolean> checkLikeExists(
            HttpServletRequest request,
            @ApiParam(value = "评价ID", required = true) @PathVariable Integer reviewId) {
        Integer userId = (Integer) request.getAttribute(JWTInterceptors.USER_ID_KEY);
        return goodsReviewService.checkLikeExists(reviewId, userId);
    }
    
    /**
     * 删除评价
     * 用户可以删除自己的评价
     * 
     * @param request HTTP请求对象，用于获取当前用户ID
     * @param reviewId 评价ID
     * @return 操作结果
     */
    @DeleteMapping("/{reviewId}")
    @ApiOperation("删除评价")
    public Result<Void> deleteReview(
            HttpServletRequest request,
            @ApiParam(value = "评价ID", required = true) @PathVariable Integer reviewId) {
        Integer userId = (Integer) request.getAttribute(JWTInterceptors.USER_ID_KEY);
        return goodsReviewService.deleteReview(reviewId, userId);
    }
    
    /**
     * 获取商品平均评分
     * 计算指定商品的所有评价的平均分
     * 
     * @param goodsId 商品ID
     * @return 平均评分
     */
    @GetMapping("/goods/{goodsId}/rating")
    @ApiOperation("获取商品平均评分")
    public Result<Double> getAverageRating(
            @ApiParam(value = "商品ID", required = true) @PathVariable Integer goodsId) {
        return goodsReviewService.getAverageRating(goodsId);
    }
    
    /**
     * 获取商品评价数量
     * 统计指定商品的评价总数
     * 
     * @param goodsId 商品ID
     * @return 评价数量
     */
    @GetMapping("/goods/{goodsId}/count")
    @ApiOperation("获取商品评价数量")
    public Result<Integer> getReviewCount(
            @ApiParam(value = "商品ID", required = true) @PathVariable Integer goodsId) {
        return goodsReviewService.getReviewCount(goodsId);
    }
    
    /**
     * 上传评价图片
     * 支持用户在上传评价时附带图片
     * 
     * @param file 图片文件
     * @return 图片访问URL
     */
    @PostMapping("/upload/image")
    @ApiOperation("上传评价图片")
    public Result<String> uploadReviewImage(
            @ApiParam(value = "图片文件", required = true) @RequestParam("file") MultipartFile file) {
        // 将图片保存到评价图片目录
        String path = "review";
        String imageUrl = fileService.uploadImage(file, path);
        return Result.success(imageUrl);
    }
} 