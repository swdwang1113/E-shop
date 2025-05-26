package ptumall.dao;

import org.apache.ibatis.annotations.Mapper;
import ptumall.model.UserAddress;
import java.util.List;

@Mapper
public interface UserAddressDao {
    /**
     * 添加收货地址
     * @param address 收货地址信息
     * @return 影响行数
     */
    int insert(UserAddress address);
    
    /**
     * 根据ID查询收货地址
     * @param id 收货地址ID
     * @return 收货地址信息
     */
    UserAddress selectById(Integer id);
    
    /**
     * 查询用户的收货地址列表
     * @param userId 用户ID
     * @return 收货地址列表
     */
    List<UserAddress> selectByUserId(Integer userId);
    
    /**
     * 更新收货地址
     * @param address 收货地址信息
     * @return 影响行数
     */
    int update(UserAddress address);
    
    /**
     * 删除收货地址
     * @param id 收货地址ID
     * @param userId 用户ID
     * @return 影响行数
     */
    int delete(Integer id, Integer userId);
    
    /**
     * 将用户所有地址设为非默认
     * @param userId 用户ID
     * @return 影响行数
     */
    int resetDefault(Integer userId);
    
    /**
     * 将指定地址设为默认
     * @param id 地址ID
     * @param userId 用户ID
     * @return 影响行数
     */
    int markDefault(Integer id, Integer userId);
} 