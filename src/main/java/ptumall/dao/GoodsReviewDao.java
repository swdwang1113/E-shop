package ptumall.dao;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import ptumall.model.GoodsReview;

import java.util.List;

@Mapper
public interface GoodsReviewDao {
    
    /**
     * 添加商品评价
     * @param goodsReview 评价信息
     * @return 影响的行数
     */
    int insert(GoodsReview goodsReview);
    
    /**
     * 根据ID查询评价
     * @param id 评价ID
     * @return 评价信息
     */
    GoodsReview findById(Integer id);
    
    /**
     * 根据商品ID查询评价列表
     * @param goodsId 商品ID
     * @return 评价列表
     */
    List<GoodsReview> findByGoodsId(Integer goodsId);
    
    /**
     * 根据用户ID查询评价列表
     * @param userId 用户ID
     * @return 评价列表
     */
    List<GoodsReview> findByUserId(Integer userId);
    
    /**
     * 根据订单ID查询评价列表
     * @param orderId 订单ID
     * @return 评价列表
     */
    List<GoodsReview> findByOrderId(Integer orderId);
    
    /**
     * 检查用户是否已经评价过订单中的商品
     * @param userId 用户ID
     * @param goodsId 商品ID
     * @param orderId 订单ID
     * @return 评价信息，如果不存在则返回null
     */
    GoodsReview findByUserIdAndGoodsIdAndOrderId(@Param("userId") Integer userId, 
                                               @Param("goodsId") Integer goodsId, 
                                               @Param("orderId") Integer orderId);
    
    /**
     * 更新评价点赞数
     * @param id 评价ID
     * @param likeCount 点赞数
     * @return 影响的行数
     */
    int updateLikeCount(@Param("id") Integer id, @Param("likeCount") Integer likeCount);
    
    /**
     * 删除评价
     * @param id 评价ID
     * @return 影响的行数
     */
    int deleteById(Integer id);
    
    /**
     * 查询商品的平均评分
     * @param goodsId 商品ID
     * @return 平均评分
     */
    Double getAverageRatingByGoodsId(Integer goodsId);
    
    /**
     * 查询商品的评价数量
     * @param goodsId 商品ID
     * @return 评价数量
     */
    Integer countByGoodsId(Integer goodsId);
} 