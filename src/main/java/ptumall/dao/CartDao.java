package ptumall.dao;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import ptumall.model.Cart;

import java.util.List;

@Mapper
public interface CartDao {
    /**
     * 根据用户ID查询购物车列表
     * @param userId 用户ID
     * @return 购物车列表
     */
    List<Cart> findByUserId(Integer userId);
    
    /**
     * 根据用户ID和商品ID查询购物车项
     * @param userId 用户ID
     * @param goodsId 商品ID
     * @return 购物车项
     */
    Cart findByUserIdAndGoodsId(@Param("userId") Integer userId, @Param("goodsId") Integer goodsId);
    
    /**
     * 添加购物车项
     * @param cart 购物车对象
     * @return 影响的行数
     */
    int insert(Cart cart);
    
    /**
     * 更新购物车项
     * @param cart 购物车对象
     * @return 影响的行数
     */
    int update(Cart cart);
    
    /**
     * 删除购物车项
     * @param id 购物车ID
     * @return 影响的行数
     */
    int deleteById(Integer id);
    
    /**
     * 根据用户ID和商品ID删除购物车项
     * @param userId 用户ID
     * @param goodsId 商品ID
     * @return 影响的行数
     */
    int deleteByUserIdAndGoodsId(@Param("userId") Integer userId, @Param("goodsId") Integer goodsId);
    
    /**
     * 清空用户购物车
     * @param userId 用户ID
     * @return 影响的行数
     */
    int clearByUserId(Integer userId);
    
    /**
     * 根据ID列表查询购物车
     * @param ids 购物车ID列表
     * @return 购物车列表
     */
    List<Cart> selectByIds(@Param("ids") List<Integer> ids);
    
    /**
     * 根据ID列表删除购物车商品
     * @param ids 购物车ID列表
     * @return 影响行数
     */
    int deleteByIds(@Param("ids") List<Integer> ids);
}
