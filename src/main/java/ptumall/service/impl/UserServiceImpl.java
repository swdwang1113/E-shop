package ptumall.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ptumall.utils.JWTUtils;
import ptumall.dao.UserDao;
import ptumall.exception.BusinessException;
import ptumall.model.User;
import ptumall.service.EmailService;
import ptumall.service.UserService;
import ptumall.service.VerificationCodeService;
import ptumall.vo.PageResult;
import ptumall.vo.ResultCode;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserDao userDao;
    
    @Autowired
    private EmailService emailService;
    
    @Autowired
    private VerificationCodeService verificationCodeService;
    
    @Autowired
    private JWTUtils jwtUtils;
    
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
    public PageResult<User> searchUsersByUsername(String username, Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<User> userList = userDao.findByUsernameLike(username);
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
    public PageResult<User> searchUsersByPhone(String phone, Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<User> userList = userDao.findByPhoneLike(phone);
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
    
    @Override
    public int getUserCount() {
        return userDao.count();
    }
    
    @Override
    public boolean sendEmailCode(String email) {
        // 检查邮箱是否已注册
        User user = userDao.findByEmail(email);
        if (user == null) {
            log.warn("邮箱未注册：{}", email);
            return false;
        }
        
        // 生成验证码
        String code = verificationCodeService.generateEmailCode(email);
        
        // 发送验证码邮件
        return emailService.sendVerificationCode(email, code);
    }
    
    @Override
    public Map<String, Object> loginByEmailCode(String email, String code) {
        // 验证验证码
        if (!verificationCodeService.verifyEmailCode(email, code)) {
            log.warn("验证码验证失败：{}", email);
            return null;
        }
        
        // 查询用户
        User user = userDao.findByEmail(email);
        if (user == null) {
            log.warn("邮箱未注册：{}", email);
            return null;
        }
        
        // 生成JWT令牌
        String token = JWTUtils.getToken(user.getId(), user.getUsername());
        
        // 构建返回结果
        Map<String, Object> result = new HashMap<>();
        result.put("userId", user.getId());
        result.put("username", user.getUsername());
        result.put("token", token);
        
        log.info("用户邮箱验证码登录成功：{}", email);
        return result;
    }
}
