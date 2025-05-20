package ptumall.dao;

import org.apache.ibatis.annotations.Mapper;
import ptumall.model.Userorder;

import java.util.List;

@Mapper
public interface OrderDao {
    int InsertOrder(Userorder userorder);
    List<Userorder> getAllOrder(Integer uid);
    int deleteOneOrder(Integer id);
    int deleteAllOrder(Integer uid);
}

