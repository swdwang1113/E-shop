package ptumall.dao;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import ptumall.model.UserFavorite;

import java.util.List;

@Mapper
public interface UserFavoriteDao {
    
    /**
     * 添加收藏
     * @param userFavorite 收藏信息
     * @return 影响的行数
     */
    int insert(UserFavorite userFavorite);
    
    /**
     * 取消收藏
     * @param userId 用户ID
     * @param goodsId 商品ID
     * @return 影响的行数
     */
    int delete(@Param("userId") Integer userId, @Param("goodsId") Integer goodsId);
    
    /**
     * 查询用户是否已收藏某商品
     * @param userId 用户ID
     * @param goodsId 商品ID
     * @return 收藏记录
     */
    UserFavorite findByUserIdAndGoodsId(@Param("userId") Integer userId, @Param("goodsId") Integer goodsId);
    
    /**
     * 查询用户的收藏列表
     * @param userId 用户ID
     * @return 收藏列表
     */
    List<UserFavorite> findByUserId(@Param("userId") Integer userId);
    
    /**
     * 查询商品的收藏数量
     * @param goodsId 商品ID
     * @return 收藏数量
     */
    int countByGoodsId(@Param("goodsId") Integer goodsId);
} 