package ptumall.dao;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import ptumall.model.ReviewLike;

@Mapper
public interface ReviewLikeDao {
    
    /**
     * 添加点赞记录
     * @param reviewLike 点赞信息
     * @return 影响的行数
     */
    int insert(ReviewLike reviewLike);
    
    /**
     * 删除点赞记录
     * @param userId 用户ID
     * @param reviewId 评价ID
     * @return 影响的行数
     */
    int deleteByUserIdAndReviewId(@Param("userId") Integer userId, @Param("reviewId") Integer reviewId);
    
    /**
     * 查询用户是否已点赞某评价
     * @param userId 用户ID
     * @param reviewId 评价ID
     * @return 点赞记录，如果不存在则返回null
     */
    ReviewLike findByUserIdAndReviewId(@Param("userId") Integer userId, @Param("reviewId") Integer reviewId);
    
    /**
     * 统计评价的点赞数
     * @param reviewId 评价ID
     * @return 点赞数
     */
    int countByReviewId(Integer reviewId);
} 