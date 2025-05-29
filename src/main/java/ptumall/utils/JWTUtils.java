package ptumall.utils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT工具类
 * 用于处理JWT（JSON Web Token）的生成和验证
 * 主要功能包括：生成token、验证token有效性
 */
@Component
@ConfigurationProperties(prefix = "jwt")
public class JWTUtils {
    /**
     * JWT签名密钥
     * 用于对token进行签名和验证
     * 从配置文件中读取
     */
    private static String SING;

    /**
     * token过期时间（分钟）
     * 从配置文件中读取
     */
    private static Integer expireTime;

    /**
     * 设置签名密钥
     * 由Spring自动注入配置值
     */
    public void setSING(String SING) {
        JWTUtils.SING = SING;
    }

    /**
     * 设置token过期时间
     * 由Spring自动注入配置值
     */
    public void setExpireTime(Integer expireTime) {
        JWTUtils.expireTime = expireTime;
    }

    /**
     * 生成JWT token
     * token包含三部分：header.payload.signature
     * 
     * @param userId 用户ID
     * @param userName 用户名
     * @return 生成的JWT token字符串
     */
    public static String getToken(Integer userId, String userName) {
        // 创建日历实例，用于设置token过期时间
        Calendar instance = Calendar.getInstance();
        // 设置过期时间，单位为分钟
        instance.add(Calendar.MINUTE, expireTime);

        // 创建JWT构建器
        JWTCreator.Builder builder = JWT.create();

        // 设置payload（负载）信息
        Map<String, String> payload = new HashMap<>();
        payload.put("userId", userId.toString());
        payload.put("name", userName);
        // 将payload信息添加到token中
        payload.forEach((k, v) -> {
            builder.withClaim(k, v);
        });

        // 生成最终的token
        // 1. 设置过期时间
        // 2. 使用HMAC256算法和密钥进行签名
        String token = builder.withExpiresAt(instance.getTime())  // 指定令牌过期时间
                .sign(Algorithm.HMAC256(SING));  // 使用密钥进行签名
        return token;
    }

    /**
     * 验证JWT token的合法性
     * 验证内容包括：
     * 1. token的签名是否正确
     * 2. token是否过期
     * 3. token的格式是否正确
     * 
     * @param token 需要验证的JWT token
     * @return 解码后的JWT对象，如果验证失败会抛出异常
     */
    public static DecodedJWT verify(String token) {
        return JWT.require(Algorithm.HMAC256(SING))  // 使用相同的密钥和算法
                .build()
                .verify(token);  // 验证token
    }
}
