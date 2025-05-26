package ptumall.service;

import ptumall.model.GoodsReview;
import ptumall.vo.Result;

import java.util.List;

public interface GoodsReviewService {
    
    /**
     * 添加商品评价
     * @param goodsReview 评价信息
     * @return 操作结果
     */
    Result<GoodsReview> addReview(GoodsReview goodsReview);
    
    /**
     * 获取商品评价列表
     * @param goodsId 商品ID
     * @return 评价列表
     */
    Result<List<GoodsReview>> getReviewsByGoodsId(Integer goodsId);
    
    /**
     * 获取用户的评价列表
     * @param userId 用户ID
     * @return 评价列表
     */
    Result<List<GoodsReview>> getReviewsByUserId(Integer userId);
    
    /**
     * 获取订单的评价列表
     * @param orderId 订单ID
     * @return 评价列表
     */
    Result<List<GoodsReview>> getReviewsByOrderId(Integer orderId);
    
    /**
     * 检查用户是否已经评价过订单中的商品
     * @param userId 用户ID
     * @param goodsId 商品ID
     * @param orderId 订单ID
     * @return 是否已评价
     */
    Result<Boolean> checkReviewExists(Integer userId, Integer goodsId, Integer orderId);
    
    /**
     * 点赞评价
     * @param reviewId 评价ID
     * @param userId 用户ID
     * @return 操作结果
     */
    Result<Void> likeReview(Integer reviewId, Integer userId);
    
    /**
     * 取消点赞评价
     * @param reviewId 评价ID
     * @param userId 用户ID
     * @return 操作结果
     */
    Result<Void> unlikeReview(Integer reviewId, Integer userId);
    
    /**
     * 检查用户是否已点赞评价
     * @param reviewId 评价ID
     * @param userId 用户ID
     * @return 是否已点赞
     */
    Result<Boolean> checkLikeExists(Integer reviewId, Integer userId);
    
    /**
     * 删除评价
     * @param reviewId 评价ID
     * @param userId 用户ID（确保只能删除自己的评价）
     * @return 操作结果
     */
    Result<Void> deleteReview(Integer reviewId, Integer userId);
    
    /**
     * 获取商品的平均评分
     * @param goodsId 商品ID
     * @return 平均评分
     */
    Result<Double> getAverageRating(Integer goodsId);
    
    /**
     * 获取商品的评价数量
     * @param goodsId 商品ID
     * @return 评价数量
     */
    Result<Integer> getReviewCount(Integer goodsId);
} 