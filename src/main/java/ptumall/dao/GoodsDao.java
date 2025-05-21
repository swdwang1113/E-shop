package ptumall.dao;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import ptumall.model.Goods;

import java.math.BigDecimal;
import java.util.List;

@Mapper
public interface GoodsDao {
    /**
     * 通过ID查询商品
     * @param id 商品ID
     * @return 商品对象
     */
    Goods findById(Integer id);
    
    /**
     * 根据ID查询商品
     * @param id 商品ID
     * @return 商品对象
     */
    Goods selectById(Integer id);
    
    /**
     * 查询商品列表
     * @param categoryId 分类ID，可为null
     * @param keyword 关键词，可为null
     * @param sortBy 排序字段，可为null，可选值：price, rating, sales, newest
     * @param sortDirection 排序方向，可为null，可选值：asc, desc
     * @return 商品列表
     */
    List<Goods> findList(@Param("categoryId") Integer categoryId, 
                          @Param("keyword") String keyword,
                          @Param("sortBy") String sortBy,
                          @Param("sortDirection") String sortDirection);
    
    /**
     * 添加商品
     * @param goods 商品对象
     * @return 影响的行数
     */
    int insert(Goods goods);
    
    /**
     * 更新商品
     * @param goods 商品对象
     * @return 影响的行数
     */
    int update(Goods goods);
    
    /**
     * 删除商品
     * @param id 商品ID
     * @return 影响的行数
     */
    int deleteById(Integer id);
    
    /**
     * 更新商品状态
     * @param id 商品ID
     * @param status 商品状态
     * @return 影响的行数
     */
    int updateStatus(@Param("id") Integer id, @Param("status") Byte status);
    
    /**
     * 查询推荐商品列表
     * @param limit 限制数量
     * @return 商品列表
     */
    List<Goods> findRecommend(@Param("limit") Integer limit);
    
    /**
     * 更新商品库存
     * @param id 商品ID
     * @param stock 更新后的库存
     * @return 影响的行数
     */
    int updateStock(@Param("id") Integer id, @Param("stock") Integer stock);
    
    /**
     * 更新商品评分
     * @param id 商品ID
     * @param rating 评分值
     * @return 影响的行数
     */
    int updateRating(@Param("id") Integer id, @Param("rating") BigDecimal rating);
    
    /**
     * 更新商品销量
     * @param id 商品ID
     * @param increment 销量增加值
     * @return 影响的行数
     */
    int updateSalesVolume(@Param("id") Integer id, @Param("increment") Integer increment);
}
