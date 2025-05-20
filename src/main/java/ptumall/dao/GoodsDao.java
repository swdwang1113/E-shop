package ptumall.dao;

import org.apache.ibatis.annotations.Mapper;
import ptumall.model.Goods;

import java.util.List;

@Mapper
public interface GoodsDao {
    List<Goods> getAllgoods();
    Goods getGoodById(Integer gid);
    List<Goods> searchGoodsByName(String gname);
}
