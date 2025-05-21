package ptumall.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ptumall.dao.UserDao;
import ptumall.exception.BusinessException;
import ptumall.model.User;
import ptumall.service.UserService;
import ptumall.vo.PageResult;
import ptumall.vo.ResultCode;

import java.util.Date;
import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserDao userDao;
    
    @Override
    @Transactional
    public User register(User user) {
        // 检查用户名是否已存在
        if (checkUsernameExists(user.getUsername())) {
            return null;
        }
        
        // 设置创建时间和更新时间
        Date now = new Date();
        user.setCreateTime(now);
        user.setUpdateTime(now);
        
        // 设置默认角色为普通用户(0)
        if (user.getRole() == null) {
            user.setRole((byte) 0);
        }
        
        // 插入用户，返回带ID的用户对象
        int rows = userDao.insert(user);
        if (rows > 0) {
            return user;
        }
        return null;
    }
    
    @Override
    public User login(String username, String password) {
        return userDao.login(username, password);
    }
    
    @Override
    public User getUserById(Integer id) {
        return userDao.findById(id);
    }
    
    @Override
    @Transactional
    public boolean updateUser(User user) {
        if (user.getId() == null) {
            return false;
        }
        
        // 如果要修改用户名，先检查新用户名是否已存在
        if (user.getUsername() != null) {
            User currentUser = userDao.findById(user.getId());
            if (currentUser == null) {
                return false;
            }
            if (!currentUser.getUsername().equals(user.getUsername()) && 
                checkUsernameExists(user.getUsername())) {
                return false;
            }
        }
        
        // 设置更新时间
        user.setUpdateTime(new Date());
        
        int rows = userDao.update(user);
        return rows > 0;
    }
    
    @Override
    public boolean checkUsernameExists(String username) {
        User user = userDao.findByUsername(username);
        return user != null;
    }
    
    @Override
    public PageResult<User> getUserList(Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<User> userList = userDao.findAll();
        PageInfo<User> pageInfo = new PageInfo<>(userList);
        
        // 隐藏密码
        for (User user : userList) {
            user.setPassword(null);
        }
        
        PageResult<User> pageResult = new PageResult<>();
        pageResult.setList(userList);
        pageResult.setTotal(pageInfo.getTotal());
        pageResult.setPages(pageInfo.getPages());
        pageResult.setPageNum(pageInfo.getPageNum());
        pageResult.setPageSize(pageInfo.getPageSize());
        
        return pageResult;
    }
    
    @Override
    @Transactional
    public boolean deleteUser(Integer id) {
        // 检查用户是否存在
        User user = userDao.findById(id);
        if (user == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "用户不存在");
        }
        
        // 不能删除管理员账户
        if (user.getRole() != null && user.getRole() == 1) {
            throw new BusinessException(ResultCode.FORBIDDEN, "不能删除管理员账户");
        }
        
        int rows = userDao.deleteById(id);
        return rows > 0;
    }
}
