package ptumall.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ptumall.dao.CartDao;
import ptumall.dao.GoodsDao;
import ptumall.model.Cart;
import ptumall.model.Goods;
import ptumall.service.CartService;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Service
public class CartServiceImpl implements CartService {

    @Autowired
    private CartDao cartDao;
    
    @Autowired
    private GoodsDao goodsDao;
    
    @Override
    public List<Cart> getCartList(Integer userId) {
        if (userId == null) {
            return null;
        }
        
        List<Cart> cartList = cartDao.findByUserId(userId);
        
        // 计算每个购物车项的总价
        if (cartList != null && !cartList.isEmpty()) {
            for (Cart cart : cartList) {
                if (cart.getGoodsPrice() != null && cart.getQuantity() != null) {
                    // 总价 = 单价 × 数量
                    BigDecimal totalPrice = cart.getGoodsPrice().multiply(new BigDecimal(cart.getQuantity()));
                    cart.setTotalPrice(totalPrice);
                }
            }
        }
        
        return cartList;
    }
    
    @Override
    @Transactional
    public boolean addToCart(Integer userId, Integer goodsId, Integer quantity) {
        if (userId == null || goodsId == null || quantity == null || quantity <= 0) {
            return false;
        }
        
        // 检查商品是否存在且上架
        Goods goods = goodsDao.findById(goodsId);
        if (goods == null || goods.getStatus() == null || goods.getStatus() != 1) {
            return false;
        }
        
        // 检查库存是否足够
        if (goods.getStock() < quantity) {
            return false;
        }
        
        // 检查购物车中是否已存在该商品
        Cart existingCart = cartDao.findByUserIdAndGoodsId(userId, goodsId);
        
        if (existingCart != null) {
            // 已存在，更新数量
            int newQuantity = existingCart.getQuantity() + quantity;
            
            // 再次检查库存是否足够
            if (goods.getStock() < newQuantity) {
                return false;
            }
            
            existingCart.setQuantity(newQuantity);
            existingCart.setUpdateTime(new Date());
            return cartDao.update(existingCart) > 0;
        } else {
            // 不存在，新增购物车项
            Cart cart = new Cart();
            cart.setUserId(userId);
            cart.setGoodsId(goodsId);
            cart.setQuantity(quantity);
            Date now = new Date();
            cart.setCreateTime(now);
            cart.setUpdateTime(now);
            return cartDao.insert(cart) > 0;
        }
    }
    
    @Override
    @Transactional
    public boolean updateQuantity(Integer userId, Integer cartId, Integer quantity) {
        if (userId == null || cartId == null || quantity == null) {
            return false;
        }
        
        if (quantity <= 0) {
            // 数量小于等于0，删除购物车项
            return deleteCartItem(userId, cartId);
        }
        
        // 先查询购物车项，确保是当前用户的
        List<Cart> carts = cartDao.findByUserId(userId);
        Cart targetCart = null;
        
        for (Cart cart : carts) {
            if (cart.getId().equals(cartId)) {
                targetCart = cart;
                break;
            }
        }
        
        if (targetCart == null) {
            return false;
        }
        
        // 检查商品是否还存在且上架
        Goods goods = goodsDao.findById(targetCart.getGoodsId());
        if (goods == null || goods.getStatus() == null || goods.getStatus() != 1) {
            return false;
        }
        
        // 检查库存是否足够
        if (goods.getStock() < quantity) {
            return false;
        }
        
        // 更新数量
        targetCart.setQuantity(quantity);
        targetCart.setUpdateTime(new Date());
        return cartDao.update(targetCart) > 0;
    }
    
    @Override
    @Transactional
    public boolean deleteCartItem(Integer userId, Integer cartId) {
        if (userId == null || cartId == null) {
            return false;
        }
        
        // 先查询购物车项，确保是当前用户的
        List<Cart> carts = cartDao.findByUserId(userId);
        boolean isUserCart = false;
        
        for (Cart cart : carts) {
            if (cart.getId().equals(cartId)) {
                isUserCart = true;
                break;
            }
        }
        
        if (!isUserCart) {
            return false;
        }
        
        return cartDao.deleteById(cartId) > 0;
    }
    
    @Override
    @Transactional
    public boolean deleteCartItemByGoodsId(Integer userId, Integer goodsId) {
        if (userId == null || goodsId == null) {
            return false;
        }
        
        return cartDao.deleteByUserIdAndGoodsId(userId, goodsId) > 0;
    }
    
    @Override
    @Transactional
    public boolean clearCart(Integer userId) {
        if (userId == null) {
            return false;
        }
        
        return cartDao.clearByUserId(userId) >= 0; // 即使没有购物车项，也认为是成功的
    }
}
