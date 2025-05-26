package ptumall.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ptumall.dao.GoodsDao;
import ptumall.dao.UserFavoriteDao;
import ptumall.model.Goods;
import ptumall.model.UserFavorite;
import ptumall.service.UserFavoriteService;
import ptumall.vo.Result;
import ptumall.vo.ResultCode;

import java.util.List;

@Service
public class UserFavoriteServiceImpl implements UserFavoriteService {
    
    @Autowired
    private UserFavoriteDao userFavoriteDao;
    
    @Autowired
    private GoodsDao goodsDao;
    
    @Override
    public Result<Void> addFavorite(Integer userId, Integer goodsId) {
        // 检查商品是否存在
        Goods goods = goodsDao.findById(goodsId);
        if (goods == null) {
            return Result.validateFailed("商品不存在");
        }
        
        // 检查是否已收藏
        UserFavorite existingFavorite = userFavoriteDao.findByUserIdAndGoodsId(userId, goodsId);
        if (existingFavorite != null) {
            return Result.validateFailed("已收藏该商品");
        }
        
        // 添加收藏
        UserFavorite userFavorite = new UserFavorite();
        userFavorite.setUserId(userId);
        userFavorite.setGoodsId(goodsId);
        
        int result = userFavoriteDao.insert(userFavorite);
        if (result > 0) {
            return Result.success();
        } else {
            return Result.failed(ResultCode.FAILED, "收藏失败");
        }
    }
    
    @Override
    public Result<Void> removeFavorite(Integer userId, Integer goodsId) {
        // 检查是否已收藏
        UserFavorite existingFavorite = userFavoriteDao.findByUserIdAndGoodsId(userId, goodsId);
        if (existingFavorite == null) {
            return Result.validateFailed("未收藏该商品");
        }
        
        // 取消收藏
        int result = userFavoriteDao.delete(userId, goodsId);
        if (result > 0) {
            return Result.success();
        } else {
            return Result.failed(ResultCode.FAILED, "取消收藏失败");
        }
    }
    
    @Override
    public Result<Boolean> isFavorite(Integer userId, Integer goodsId) {
        UserFavorite userFavorite = userFavoriteDao.findByUserIdAndGoodsId(userId, goodsId);
        return Result.success(userFavorite != null);
    }
    
    @Override
    public Result<List<UserFavorite>> getFavoriteList(Integer userId) {
        List<UserFavorite> favoriteList = userFavoriteDao.findByUserId(userId);
        return Result.success(favoriteList);
    }
} 