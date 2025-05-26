package ptumall.vo;

/**
 * 返回码枚举类
 */
public enum ResultCode {
    /**
     * 成功
     */
    SUCCESS(200, "成功"),
    
    /**
     * 失败
     */
    FAILED(500, "失败"),
    
    /**
     * 参数错误
     */
    PARAM_ERROR(400, "参数错误"),
    
    /**
     * 未授权
     */
    UNAUTHORIZED(401, "未授权"),
    
    /**
     * 禁止访问
     */
    FORBIDDEN(403, "禁止访问"),
    
    /**
     * 资源不存在
     */
    NOT_FOUND(404, "资源不存在"),
    
    /**
     * 请求方法不允许
     */
    METHOD_NOT_ALLOWED(405, "方法不被允许"),
    
    /**
     * 服务器内部错误
     */
    INTERNAL_SERVER_ERROR(500, "系统繁忙"),
    
    /**
     * Bad Request
     */
    BAD_REQUEST(400, "Bad Request"),
    
    /**
     * 用户不存在
     */
    USER_NOT_EXIST(1001, "用户不存在"),
    
    /**
     * 用户名或密码错误
     */
    USERNAME_OR_PASSWORD_ERROR(1002, "用户名或密码错误"),
    
    /**
     * 用户已存在
     */
    USER_ALREADY_EXIST(1003, "用户已存在"),
    
    /**
     * 参数无效
     */
    PARAMS_IS_INVALID(1001, "参数无效"),
    
    /**
     * 参数为空
     */
    PARAMS_IS_BLANK(1002, "参数为空"),
    
    /**
     * 用户名已存在
     */
    USER_IS_EXITES(1003, "用户名已存在");
    
    private int code;
    private String message;
    
    ResultCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
    
    public int getCode() {
        return code;
    }
    
    public String getMessage() {
        return message;
    }
} 