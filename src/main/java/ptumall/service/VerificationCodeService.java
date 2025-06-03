package ptumall.service;

/**
 * 验证码服务接口
 */
public interface VerificationCodeService {
    /**
     * 生成邮箱验证码
     * @param email 邮箱
     * @return 验证码
     */
    String generateEmailCode(String email);
    
    /**
     * 验证邮箱验证码
     * @param email 邮箱
     * @param code 验证码
     * @return 是否验证成功
     */
    boolean verifyEmailCode(String email, String code);
} 