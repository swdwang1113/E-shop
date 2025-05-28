package ptumall.service;

import ptumall.model.User;
import ptumall.vo.PageResult;

public interface UserService {
    /**
     * 用户注册
     * @param user 用户信息
     * @return 注册成功返回用户对象，失败返回null
     */
    User register(User user);
    
    /**
     * 用户登录
     * @param username 用户名
     * @param password 密码
     * @return 登录成功返回用户对象，失败返回null
     */
    User login(String username, String password);
    
    /**
     * 根据ID查询用户
     * @param id 用户ID
     * @return 用户对象
     */
    User getUserById(Integer id);
    
    /**
     * 更新用户信息
     * @param user 用户信息
     * @return 更新成功返回true，失败返回false
     */
    boolean updateUser(User user);
    
    /**
     * 检查用户名是否已存在
     * @param username 用户名
     * @return 存在返回true，不存在返回false
     */
    boolean checkUsernameExists(String username);
    
    /**
     * 获取用户列表（分页）
     * @param pageNum 页码
     * @param pageSize 每页数量
     * @return 用户列表
     */
    PageResult<User> getUserList(Integer pageNum, Integer pageSize);
    
    /**
     * 根据用户名搜索用户（分页）
     * @param username 用户名关键词
     * @param pageNum 页码
     * @param pageSize 每页数量
     * @return 用户列表
     */
    PageResult<User> searchUsersByUsername(String username, Integer pageNum, Integer pageSize);
    
    /**
     * 根据手机号搜索用户（分页）
     * @param phone 手机号关键词
     * @param pageNum 页码
     * @param pageSize 每页数量
     * @return 用户列表
     */
    PageResult<User> searchUsersByPhone(String phone, Integer pageNum, Integer pageSize);
    
    /**
     * 删除用户
     * @param id 用户ID
     * @return 删除成功返回true，失败返回false
     */
    boolean deleteUser(Integer id);
    
    /**
     * 获取用户总数
     * @return 用户总数
     */
    int getUserCount();
}
