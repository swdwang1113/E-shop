package ptumall.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ptumall.dao.GoodsDao;
import ptumall.dao.GoodsReviewDao;
import ptumall.dao.OrderDao;
import ptumall.dao.ReviewLikeDao;
import ptumall.model.Goods;
import ptumall.model.GoodsReview;
import ptumall.model.Orders;
import ptumall.model.ReviewLike;
import ptumall.service.GoodsReviewService;
import ptumall.vo.Result;
import ptumall.vo.ResultCode;

import java.util.List;

@Service
public class GoodsReviewServiceImpl implements GoodsReviewService {
    
    @Autowired
    private GoodsReviewDao goodsReviewDao;
    
    @Autowired
    private GoodsDao goodsDao;
    
    @Autowired
    private OrderDao orderDao;
    
    @Autowired
    private ReviewLikeDao reviewLikeDao;
    
    @Override
    public Result<GoodsReview> addReview(GoodsReview goodsReview) {
        // 检查商品是否存在
        Goods goods = goodsDao.findById(goodsReview.getGoodsId());
        if (goods == null) {
            return Result.validateFailed("商品不存在");
        }
        
        // 检查订单是否存在且属于当前用户
        Orders order = orderDao.selectById(goodsReview.getOrderId());
        if (order == null) {
            return Result.validateFailed("订单不存在");
        }
        if (!order.getUserId().equals(goodsReview.getUserId())) {
            return Result.validateFailed("无权评价此订单");
        }
        
        // 检查订单状态是否为已完成
        if (order.getStatus() != 3) { // 3表示已完成
            return Result.validateFailed("只能评价已完成的订单");
        }
        
        // 检查是否已经评价过
        GoodsReview existingReview = goodsReviewDao.findByUserIdAndGoodsIdAndOrderId(
                goodsReview.getUserId(), goodsReview.getGoodsId(), goodsReview.getOrderId());
        if (existingReview != null) {
            return Result.validateFailed("已经评价过此商品");
        }
        
        // 设置初始点赞数为0
        goodsReview.setLikeCount(0);
        
        // 添加评价
        int result = goodsReviewDao.insert(goodsReview);
        if (result > 0) {
            // 查询刚插入的评价，包含用户名和商品名称
            GoodsReview insertedReview = goodsReviewDao.findById(goodsReview.getId());
            return Result.success(insertedReview);
        } else {
            return Result.failed(ResultCode.FAILED, "评价失败");
        }
    }
    
    @Override
    public Result<List<GoodsReview>> getReviewsByGoodsId(Integer goodsId) {
        // 检查商品是否存在
        Goods goods = goodsDao.findById(goodsId);
        if (goods == null) {
            return Result.validateFailed("商品不存在");
        }
        
        List<GoodsReview> reviews = goodsReviewDao.findByGoodsId(goodsId);
        return Result.success(reviews);
    }
    
    @Override
    public Result<List<GoodsReview>> getReviewsByUserId(Integer userId) {
        List<GoodsReview> reviews = goodsReviewDao.findByUserId(userId);
        return Result.success(reviews);
    }
    
    @Override
    public Result<List<GoodsReview>> getReviewsByOrderId(Integer orderId) {
        // 检查订单是否存在
        Orders order = orderDao.selectById(orderId);
        if (order == null) {
            return Result.validateFailed("订单不存在");
        }
        
        List<GoodsReview> reviews = goodsReviewDao.findByOrderId(orderId);
        return Result.success(reviews);
    }
    
    @Override
    public Result<Boolean> checkReviewExists(Integer userId, Integer goodsId, Integer orderId) {
        GoodsReview review = goodsReviewDao.findByUserIdAndGoodsIdAndOrderId(userId, goodsId, orderId);
        return Result.success(review != null);
    }
    
    @Override
    @Transactional
    public Result<Void> likeReview(Integer reviewId, Integer userId) {
        // 检查评价是否存在
        GoodsReview review = goodsReviewDao.findById(reviewId);
        if (review == null) {
            return Result.validateFailed("评价不存在");
        }
        
        // 检查用户是否已点赞
        ReviewLike existingLike = reviewLikeDao.findByUserIdAndReviewId(userId, reviewId);
        if (existingLike != null) {
            return Result.validateFailed("已经点赞过此评价");
        }
        
        // 添加点赞记录
        ReviewLike reviewLike = new ReviewLike();
        reviewLike.setUserId(userId);
        reviewLike.setReviewId(reviewId);
        int result = reviewLikeDao.insert(reviewLike);
        
        if (result > 0) {
            // 更新评价的点赞数
            int likeCount = reviewLikeDao.countByReviewId(reviewId);
            goodsReviewDao.updateLikeCount(reviewId, likeCount);
            return Result.success();
        } else {
            return Result.failed(ResultCode.FAILED, "点赞失败");
        }
    }
    
    @Override
    @Transactional
    public Result<Void> unlikeReview(Integer reviewId, Integer userId) {
        // 检查评价是否存在
        GoodsReview review = goodsReviewDao.findById(reviewId);
        if (review == null) {
            return Result.validateFailed("评价不存在");
        }
        
        // 检查用户是否已点赞
        ReviewLike existingLike = reviewLikeDao.findByUserIdAndReviewId(userId, reviewId);
        if (existingLike == null) {
            return Result.validateFailed("未点赞此评价");
        }
        
        // 删除点赞记录
        int result = reviewLikeDao.deleteByUserIdAndReviewId(userId, reviewId);
        
        if (result > 0) {
            // 更新评价的点赞数
            int likeCount = reviewLikeDao.countByReviewId(reviewId);
            goodsReviewDao.updateLikeCount(reviewId, likeCount);
            return Result.success();
        } else {
            return Result.failed(ResultCode.FAILED, "取消点赞失败");
        }
    }
    
    @Override
    public Result<Boolean> checkLikeExists(Integer reviewId, Integer userId) {
        ReviewLike reviewLike = reviewLikeDao.findByUserIdAndReviewId(userId, reviewId);
        return Result.success(reviewLike != null);
    }
    
    @Override
    public Result<Void> deleteReview(Integer reviewId, Integer userId) {
        // 检查评价是否存在
        GoodsReview review = goodsReviewDao.findById(reviewId);
        if (review == null) {
            return Result.validateFailed("评价不存在");
        }
        
        // 检查是否是自己的评价
        if (!review.getUserId().equals(userId)) {
            return Result.validateFailed("无权删除此评价");
        }
        
        int result = goodsReviewDao.deleteById(reviewId);
        if (result > 0) {
            return Result.success();
        } else {
            return Result.failed(ResultCode.FAILED, "删除失败");
        }
    }
    
    @Override
    public Result<Double> getAverageRating(Integer goodsId) {
        // 检查商品是否存在
        Goods goods = goodsDao.findById(goodsId);
        if (goods == null) {
            return Result.validateFailed("商品不存在");
        }
        
        Double averageRating = goodsReviewDao.getAverageRatingByGoodsId(goodsId);
        // 如果没有评价，返回0
        if (averageRating == null) {
            averageRating = 0.0;
        }
        return Result.success(averageRating);
    }
    
    @Override
    public Result<Integer> getReviewCount(Integer goodsId) {
        // 检查商品是否存在
        Goods goods = goodsDao.findById(goodsId);
        if (goods == null) {
            return Result.validateFailed("商品不存在");
        }
        
        Integer count = goodsReviewDao.countByGoodsId(goodsId);
        return Result.success(count);
    }
} 