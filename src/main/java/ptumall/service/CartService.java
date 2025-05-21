package ptumall.service;

import ptumall.model.Cart;
import java.util.List;

public interface CartService {
    /**
     * 获取用户购物车列表
     * @param userId 用户ID
     * @return 购物车列表
     */
    List<Cart> getCartList(Integer userId);
    
    /**
     * 添加商品到购物车
     * @param userId 用户ID
     * @param goodsId 商品ID
     * @param quantity 数量
     * @return 添加结果，true表示成功，false表示失败
     */
    boolean addToCart(Integer userId, Integer goodsId, Integer quantity);
    
    /**
     * 更新购物车商品数量
     * @param userId 用户ID
     * @param cartId 购物车ID
     * @param quantity 新的数量
     * @return 更新结果，true表示成功，false表示失败
     */
    boolean updateQuantity(Integer userId, Integer cartId, Integer quantity);
    
    /**
     * 删除购物车商品
     * @param userId 用户ID
     * @param cartId 购物车ID
     * @return 删除结果，true表示成功，false表示失败
     */
    boolean deleteCartItem(Integer userId, Integer cartId);
    
    /**
     * 根据商品ID删除购物车商品
     * @param userId 用户ID
     * @param goodsId 商品ID
     * @return 删除结果，true表示成功，false表示失败
     */
    boolean deleteCartItemByGoodsId(Integer userId, Integer goodsId);
    
    /**
     * 清空用户购物车
     * @param userId 用户ID
     * @return 清空结果，true表示成功，false表示失败
     */
    boolean clearCart(Integer userId);
}
