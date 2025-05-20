package ptumall.service;

import org.springframework.stereotype.Service;
import ptumall.model.User;

@Service
public interface UserService {
    //注册
    User registerService(User user);
    //登录
    User loginService(String uname,String upassword);
}
