package ptumall.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import ptumall.service.VerificationCodeService;

import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * 验证码服务实现类
 */
@Slf4j
@Service
public class VerificationCodeServiceImpl implements VerificationCodeService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    // 验证码Redis前缀
    private static final String EMAIL_CODE_PREFIX = "email:code:";
    // 验证码有效期（分钟）
    private static final long CODE_EXPIRE_MINUTES = 5;
    
    @Override
    public String generateEmailCode(String email) {
        // 生成6位数字验证码
        String code = String.format("%06d", new Random().nextInt(1000000));
        
        // 存入Redis，设置过期时间
        String key = EMAIL_CODE_PREFIX + email;
        redisTemplate.opsForValue().set(key, code, CODE_EXPIRE_MINUTES, TimeUnit.MINUTES);
        
        log.info("生成邮箱验证码：{} -> {}", email, code);
        return code;
    }
    
    @Override
    public boolean verifyEmailCode(String email, String code) {
        String key = EMAIL_CODE_PREFIX + email;
        Object storedCode = redisTemplate.opsForValue().get(key);
        
        if (storedCode != null && storedCode.toString().equals(code)) {
            // 验证成功后删除验证码
            redisTemplate.delete(key);
            log.info("验证邮箱验证码成功：{}", email);
            return true;
        }
        
        log.warn("验证邮箱验证码失败：{} -> 输入：{}，实际：{}", email, code, storedCode);
        return false;
    }
} 