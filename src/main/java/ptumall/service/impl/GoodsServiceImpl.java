package ptumall.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ptumall.dao.GoodsDao;
import ptumall.model.Goods;
import ptumall.service.GoodsService;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Service
public class GoodsServiceImpl implements GoodsService {

    @Autowired
    private GoodsDao goodsDao;
    
    @Override
    public Goods getGoodsById(Integer id) {
        return goodsDao.findById(id);
    }
    
    @Override
    public List<Goods> getGoodsList(Integer categoryId, String keyword, String sortBy, String sortDirection) {
        return goodsDao.findList(categoryId, keyword, sortBy, sortDirection);
    }
    
    @Override
    @Transactional
    public Goods addGoods(Goods goods) {
        // 设置创建时间和更新时间
        Date now = new Date();
        goods.setCreateTime(now);
        goods.setUpdateTime(now);
        
        // 默认设置状态为下架
        if (goods.getStatus() == null) {
            goods.setStatus((byte) 0);
        }
        
        // 设置默认评分和销量
        if (goods.getRating() == null) {
            goods.setRating(new BigDecimal("0.0"));
        }
        
        if (goods.getSalesVolume() == null) {
            goods.setSalesVolume(0);
        }
        
        int rows = goodsDao.insert(goods);
        if (rows > 0) {
            return goods;
        }
        return null;
    }
    
    @Override
    @Transactional
    public boolean updateGoods(Goods goods) {
        if (goods.getId() == null) {
            return false;
        }
        
        // 设置更新时间
        goods.setUpdateTime(new Date());
        
        int rows = goodsDao.update(goods);
        return rows > 0;
    }
    
    @Override
    @Transactional
    public boolean deleteGoods(Integer id) {
        int rows = goodsDao.deleteById(id);
        return rows > 0;
    }
    
    @Override
    @Transactional
    public boolean updateStatus(Integer id, Byte status) {
        int rows = goodsDao.updateStatus(id, status);
        return rows > 0;
    }
    
    @Override
    public List<Goods> getRecommendGoods(Integer limit) {
        if (limit == null || limit <= 0) {
            limit = 10; // 默认返回10条推荐商品
        }
        return goodsDao.findRecommend(limit);
    }
    
    @Override
    @Transactional
    public boolean updateStock(Integer id, Integer stock) {
        if (id == null || stock == null || stock < 0) {
            return false;
        }
        
        int rows = goodsDao.updateStock(id, stock);
        return rows > 0;
    }
    
    @Override
    @Transactional
    public boolean updateRating(Integer id, BigDecimal rating) {
        if (id == null || rating == null || rating.compareTo(BigDecimal.ZERO) < 0 || rating.compareTo(new BigDecimal("5")) > 0) {
            return false;
        }
        
        int rows = goodsDao.updateRating(id, rating);
        return rows > 0;
    }
    
    @Override
    @Transactional
    public boolean updateSalesVolume(Integer id, Integer increment) {
        if (id == null || increment == null || increment <= 0) {
            return false;
        }
        
        int rows = goodsDao.updateSalesVolume(id, increment);
        return rows > 0;
    }
}
