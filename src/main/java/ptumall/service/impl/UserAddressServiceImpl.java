package ptumall.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ptumall.dao.UserAddressDao;
import ptumall.exception.BusinessException;
import ptumall.model.UserAddress;
import ptumall.service.UserAddressService;
import ptumall.vo.ResultCode;

import java.util.Date;
import java.util.List;

@Service
public class UserAddressServiceImpl implements UserAddressService {

    @Autowired
    private UserAddressDao userAddressDao;

    /**
     * 添加收货地址
     * @param address 收货地址信息
     * @return 添加后的收货地址
     */
    @Override
    @Transactional
    public UserAddress addAddress(UserAddress address) {
        // 如果是默认地址，先将该用户的所有地址设为非默认
        if (address.getIsDefault() != null && address.getIsDefault() == 1) {
            List<UserAddress> addressList = userAddressDao.selectByUserId(address.getUserId());
            if (addressList != null && !addressList.isEmpty()) {
                for (UserAddress existingAddress : addressList) {
                    existingAddress.setIsDefault((byte) 0);
                    userAddressDao.update(existingAddress);
                }
            }
        }
        
        // 如果是第一个地址，则设为默认地址
        if (address.getIsDefault() == null) {
            List<UserAddress> addressList = userAddressDao.selectByUserId(address.getUserId());
            if (addressList == null || addressList.isEmpty()) {
                address.setIsDefault((byte) 1);
            } else {
                address.setIsDefault((byte) 0);
            }
        }
        
        // 设置创建时间和更新时间
        Date now = new Date();
        address.setCreateTime(now);
        address.setUpdateTime(now);
        
        // 保存地址
        userAddressDao.insert(address);
        
        return address;
    }

    /**
     * 获取用户的收货地址列表
     * @param userId 用户ID
     * @return 收货地址列表
     */
    @Override
    public List<UserAddress> getAddressList(Integer userId) {
        return userAddressDao.selectByUserId(userId);
    }

    /**
     * 获取收货地址详情
     * @param userId 用户ID
     * @param addressId 地址ID
     * @return 收货地址详情
     */
    @Override
    public UserAddress getAddress(Integer userId, Integer addressId) {
        UserAddress address = userAddressDao.selectById(addressId);
        if (address == null || !address.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "收货地址不存在");
        }
        return address;
    }

    /**
     * 更新收货地址
     * @param address 收货地址信息
     * @return 更新后的收货地址
     */
    @Override
    @Transactional
    public UserAddress updateAddress(UserAddress address) {
        // 校验地址是否存在
        UserAddress existingAddress = userAddressDao.selectById(address.getId());
        if (existingAddress == null || !existingAddress.getUserId().equals(address.getUserId())) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "收货地址不存在");
        }
        
        // 如果设为默认地址，先将该用户的所有地址设为非默认
        if (address.getIsDefault() != null && address.getIsDefault() == 1) {
            userAddressDao.resetDefault(address.getUserId());
            userAddressDao.markDefault(address.getId(), address.getUserId());
        }
        
        // 设置更新时间
        address.setUpdateTime(new Date());
        
        // 更新地址
        userAddressDao.update(address);
        
        return userAddressDao.selectById(address.getId());
    }

    /**
     * 删除收货地址
     * @param userId 用户ID
     * @param addressId 地址ID
     * @return 是否成功
     */
    @Override
    @Transactional
    public boolean deleteAddress(Integer userId, Integer addressId) {
        // 校验地址是否存在
        UserAddress address = userAddressDao.selectById(addressId);
        if (address == null || !address.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "收货地址不存在");
        }
        
        // 如果删除的是默认地址，则将第一个地址设为默认
        if (address.getIsDefault() == 1) {
            List<UserAddress> addressList = userAddressDao.selectByUserId(userId);
            if (addressList.size() > 1) {
                for (UserAddress userAddress : addressList) {
                    if (!userAddress.getId().equals(addressId)) {
                        userAddressDao.resetDefault(userId);
                        userAddressDao.markDefault(userAddress.getId(), userId);
                        break;
                    }
                }
            }
        }
        
        // 删除地址
        return userAddressDao.delete(addressId, userId) > 0;
    }

    /**
     * 设为默认地址
     * @param userId 用户ID
     * @param addressId 地址ID
     * @return 是否成功
     */
    @Override
    @Transactional
    public boolean setDefaultAddress(Integer userId, Integer addressId) {
        // 校验地址是否存在
        UserAddress address = userAddressDao.selectById(addressId);
        if (address == null || !address.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "收货地址不存在");
        }
        
        // 将所有地址设为非默认，再将当前地址设为默认
        userAddressDao.resetDefault(userId);
        return userAddressDao.markDefault(addressId, userId) > 0;
    }
} 