package ptumall.service;

import ptumall.model.Goods;
import java.math.BigDecimal;
import java.util.List;

public interface GoodsService {
    /**
     * 根据ID查询商品
     * @param id 商品ID
     * @return 商品对象
     */
    Goods getGoodsById(Integer id);
    
    /**
     * 查询商品列表
     * @param categoryId 分类ID，可为null
     * @param keyword 关键词，可为null
     * @param sortBy 排序字段，可为null
     * @param sortDirection 排序方向，可为null
     * @return 商品列表
     */
    List<Goods> getGoodsList(Integer categoryId, String keyword, String sortBy, String sortDirection);
    
    /**
     * 添加商品
     * @param goods 商品对象
     * @return 添加后的商品对象（包含ID）
     */
    Goods addGoods(Goods goods);
    
    /**
     * 更新商品
     * @param goods 商品对象
     * @return 是否成功
     */
    boolean updateGoods(Goods goods);
    
    /**
     * 删除商品
     * @param id 商品ID
     * @return 是否成功
     */
    boolean deleteGoods(Integer id);
    
    /**
     * 更新商品状态（上架/下架）
     * @param id 商品ID
     * @param status 状态，1-上架，0-下架
     * @return 是否成功
     */
    boolean updateStatus(Integer id, Byte status);
    
    /**
     * 获取推荐商品
     * @param limit 限制数量
     * @return 商品列表
     */
    List<Goods> getRecommendGoods(Integer limit);
    
    /**
     * 更新商品库存
     * @param id 商品ID
     * @param stock 更新后的库存
     * @return 是否成功
     */
    boolean updateStock(Integer id, Integer stock);
    
    /**
     * 更新商品评分
     * @param id 商品ID
     * @param rating 评分值
     * @return 是否成功
     */
    boolean updateRating(Integer id, BigDecimal rating);
    
    /**
     * 更新商品销量
     * @param id 商品ID
     * @param increment 销量增加值
     * @return 是否成功
     */
    boolean updateSalesVolume(Integer id, Integer increment);
}
