package ptumall.dao;

import org.apache.ibatis.annotations.Mapper;
import ptumall.model.User;

import java.util.List;

@Mapper
public interface UserDao {
    /**
     * 通过用户名查询用户
     * @param username 用户名
     * @return 用户对象，未找到则返回null
     */
    User findByUsername(String username);
    
    /**
     * 插入新用户
     * @param user 用户对象
     * @return 影响的行数
     */
    int insert(User user);
    
    /**
     * 用户登录
     * @param username 用户名
     * @param password 密码
     * @return 用户对象，验证失败则返回null
     */
    User login(String username, String password);
    
    /**
     * 根据ID查询用户
     * @param id 用户ID
     * @return 用户对象，未找到则返回null
     */
    User findById(Integer id);
    
    /**
     * 更新用户信息
     * @param user 用户对象
     * @return 影响的行数
     */
    int update(User user);
    
    /**
     * 获取所有用户列表
     * @return 用户列表
     */
    List<User> findAll();
    
    /**
     * 根据用户名模糊搜索用户
     * @param username 用户名关键词
     * @return 用户列表
     */
    List<User> findByUsernameLike(String username);
    
    /**
     * 根据手机号模糊搜索用户
     * @param phone 手机号关键词
     * @return 用户列表
     */
    List<User> findByPhoneLike(String phone);
    
    /**
     * 删除用户
     * @param id 用户ID
     * @return 影响的行数
     */
    int deleteById(Integer id);
    
    /**
     * 获取总用户数
     * @return 用户数量
     */
    int count();
    
    /**
     * 根据邮箱查询用户
     * @param email 邮箱
     * @return 用户对象，未找到则返回null
     */
    User findByEmail(String email);
}
