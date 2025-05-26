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

@RestController
@RequestMapping("/api/reviews")
@Api(tags = "商品评价接口")
public class GoodsReviewController {
    
    @Autowired
    private GoodsReviewService goodsReviewService;
    
    @Autowired
    private FileService fileService;
    
    @PostMapping
    @ApiOperation("添加商品评价")
    public Result<GoodsReview> addReview(
            HttpServletRequest request,
            @RequestBody GoodsReview goodsReview) {
        Integer userId = (Integer) request.getAttribute(JWTInterceptors.USER_ID_KEY);
        goodsReview.setUserId(userId);
        return goodsReviewService.addReview(goodsReview);
    }
    
    @GetMapping("/goods/{goodsId}")
    @ApiOperation("获取商品评价列表")
    public Result<List<GoodsReview>> getReviewsByGoodsId(
            @ApiParam(value = "商品ID", required = true) @PathVariable Integer goodsId) {
        return goodsReviewService.getReviewsByGoodsId(goodsId);
    }
    
    @GetMapping("/user")
    @ApiOperation("获取用户的评价列表")
    public Result<List<GoodsReview>> getReviewsByUserId(HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute(JWTInterceptors.USER_ID_KEY);
        return goodsReviewService.getReviewsByUserId(userId);
    }
    
    @GetMapping("/order/{orderId}")
    @ApiOperation("获取订单的评价列表")
    public Result<List<GoodsReview>> getReviewsByOrderId(
            HttpServletRequest request,
            @ApiParam(value = "订单ID", required = true) @PathVariable Integer orderId) {
        Integer userId = (Integer) request.getAttribute(JWTInterceptors.USER_ID_KEY);
        // 这里可以增加权限检查，确保用户只能查看自己的订单评价
        return goodsReviewService.getReviewsByOrderId(orderId);
    }
    
    @GetMapping("/check")
    @ApiOperation("检查是否已评价")
    public Result<Boolean> checkReviewExists(
            HttpServletRequest request,
            @ApiParam(value = "商品ID", required = true) @RequestParam Integer goodsId,
            @ApiParam(value = "订单ID", required = true) @RequestParam Integer orderId) {
        Integer userId = (Integer) request.getAttribute(JWTInterceptors.USER_ID_KEY);
        return goodsReviewService.checkReviewExists(userId, goodsId, orderId);
    }
    
    @PostMapping("/{reviewId}/like")
    @ApiOperation("点赞评价")
    public Result<Void> likeReview(
            HttpServletRequest request,
            @ApiParam(value = "评价ID", required = true) @PathVariable Integer reviewId) {
        Integer userId = (Integer) request.getAttribute(JWTInterceptors.USER_ID_KEY);
        return goodsReviewService.likeReview(reviewId, userId);
    }
    
    @DeleteMapping("/{reviewId}/like")
    @ApiOperation("取消点赞评价")
    public Result<Void> unlikeReview(
            HttpServletRequest request,
            @ApiParam(value = "评价ID", required = true) @PathVariable Integer reviewId) {
        Integer userId = (Integer) request.getAttribute(JWTInterceptors.USER_ID_KEY);
        return goodsReviewService.unlikeReview(reviewId, userId);
    }
    
    @GetMapping("/{reviewId}/like/check")
    @ApiOperation("检查是否已点赞")
    public Result<Boolean> checkLikeExists(
            HttpServletRequest request,
            @ApiParam(value = "评价ID", required = true) @PathVariable Integer reviewId) {
        Integer userId = (Integer) request.getAttribute(JWTInterceptors.USER_ID_KEY);
        return goodsReviewService.checkLikeExists(reviewId, userId);
    }
    
    @DeleteMapping("/{reviewId}")
    @ApiOperation("删除评价")
    public Result<Void> deleteReview(
            HttpServletRequest request,
            @ApiParam(value = "评价ID", required = true) @PathVariable Integer reviewId) {
        Integer userId = (Integer) request.getAttribute(JWTInterceptors.USER_ID_KEY);
        return goodsReviewService.deleteReview(reviewId, userId);
    }
    
    @GetMapping("/goods/{goodsId}/rating")
    @ApiOperation("获取商品平均评分")
    public Result<Double> getAverageRating(
            @ApiParam(value = "商品ID", required = true) @PathVariable Integer goodsId) {
        return goodsReviewService.getAverageRating(goodsId);
    }
    
    @GetMapping("/goods/{goodsId}/count")
    @ApiOperation("获取商品评价数量")
    public Result<Integer> getReviewCount(
            @ApiParam(value = "商品ID", required = true) @PathVariable Integer goodsId) {
        return goodsReviewService.getReviewCount(goodsId);
    }
    
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