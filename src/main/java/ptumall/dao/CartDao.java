package ptumall.dao;

import org.apache.ibatis.annotations.Mapper;
import ptumall.model.Cart;

import java.util.List;

@Mapper
public interface CartDao {
    int insertCart(Cart cart);

    Cart getCartByUGid(Integer uid,Integer gid);

    int updateCart(Integer number,Integer price,Integer id);

    List<Cart> getAllCart(Integer uid);

    int deleteOne(Integer id);
    int deleteAll(Integer uid);
}
