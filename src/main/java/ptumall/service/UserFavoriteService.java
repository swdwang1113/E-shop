package ptumall.service;

import ptumall.model.UserFavorite;
import ptumall.vo.Result;

import java.util.List;

public interface UserFavoriteService {
    
    /**
     * 添加收藏
     * @param userId 用户ID
     * @param goodsId 商品ID
     * @return 操作结果
     */
    Result<Void> addFavorite(Integer userId, Integer goodsId);
    
    /**
     * 取消收藏
     * @param userId 用户ID
     * @param goodsId 商品ID
     * @return 操作结果
     */
    Result<Void> removeFavorite(Integer userId, Integer goodsId);
    
    /**
     * 查询用户是否已收藏某商品
     * @param userId 用户ID
     * @param goodsId 商品ID
     * @return 是否已收藏
     */
    Result<Boolean> isFavorite(Integer userId, Integer goodsId);
    
    /**
     * 查询用户的收藏列表
     * @param userId 用户ID
     * @return 收藏列表
     */
    Result<List<UserFavorite>> getFavoriteList(Integer userId);
} 