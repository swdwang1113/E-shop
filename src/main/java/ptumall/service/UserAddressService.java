package ptumall.service;

import ptumall.model.UserAddress;
import java.util.List;

/**
 * 用户地址服务接口
 */
public interface UserAddressService {
    /**
     * 添加收货地址
     * @param address 收货地址信息
     * @return 添加后的收货地址
     */
    UserAddress addAddress(UserAddress address);
    
    /**
     * 获取用户的收货地址列表
     * @param userId 用户ID
     * @return 收货地址列表
     */
    List<UserAddress> getAddressList(Integer userId);
    
    /**
     * 获取收货地址详情
     * @param userId 用户ID
     * @param addressId 地址ID
     * @return 收货地址详情
     */
    UserAddress getAddress(Integer userId, Integer addressId);
    
    /**
     * 更新收货地址
     * @param address 收货地址信息
     * @return 更新后的收货地址
     */
    UserAddress updateAddress(UserAddress address);
    
    /**
     * 删除收货地址
     * @param userId 用户ID
     * @param addressId 地址ID
     * @return 是否成功
     */
    boolean deleteAddress(Integer userId, Integer addressId);
    
    /**
     * 设为默认地址
     * @param userId 用户ID
     * @param addressId 地址ID
     * @return 是否成功
     */
    boolean setDefaultAddress(Integer userId, Integer addressId);
} 