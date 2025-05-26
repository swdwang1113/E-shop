package ptumall.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel("返回结果")
public class Result<T> implements Serializable {
    @ApiModelProperty(value = "是否成功", example = "true")
    private Boolean success;
    
    @ApiModelProperty(value = "状态码", example = "200")
    private int code;
    
    @ApiModelProperty(value = "状态信息", example = "成功")
    private String message;
    
    @ApiModelProperty(value = "返回数据")
    private T data;
    
    /**
     * 有参构造器（不含success字段）
     */
    public Result(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
        this.success = code == ResultCode.SUCCESS.getCode();
    }
    
    /**
     * 通用返回成功（没有返回结果）
     * @param <T> 数据类型
     * @return 返回结果
     */
    public static <T> Result<T> success() {
        return new Result<>(true, ResultCode.SUCCESS.getCode(), ResultCode.SUCCESS.getMessage(), null);
    }
    
    /**
     * 成功返回结果
     * @param data 返回数据
     * @param <T> 数据类型
     * @return 返回结果
     */
    public static <T> Result<T> success(T data) {
        return new Result<>(true, ResultCode.SUCCESS.getCode(), ResultCode.SUCCESS.getMessage(), data);
    }
    
    /**
     * 成功返回结果
     * @param message 提示信息
     * @param data 返回数据
     * @param <T> 数据类型
     * @return 返回结果
     */
    public static <T> Result<T> success(String message, T data) {
        return new Result<>(true, ResultCode.SUCCESS.getCode(), message, data);
    }
    
    /**
     * 失败返回结果
     * @param resultCode 结果码
     * @param <T> 数据类型
     * @return 返回结果
     */
    public static <T> Result<T> failed(ResultCode resultCode) {
        return new Result<>(false, resultCode.getCode(), resultCode.getMessage(), null);
    }
    
    /**
     * 失败返回结果
     * @param resultCode 结果码
     * @param message 提示信息
     * @param <T> 数据类型
     * @return 返回结果
     */
    public static <T> Result<T> failed(ResultCode resultCode, String message) {
        return new Result<>(false, resultCode.getCode(), message, null);
    }
    
    /**
     * 参数验证失败返回结果
     * @param <T> 数据类型
     * @return 返回结果
     */
    public static <T> Result<T> validateFailed() {
        return failed(ResultCode.PARAM_ERROR);
    }
    
    /**
     * 参数验证失败返回结果
     * @param message 提示信息
     * @param <T> 数据类型
     * @return 返回结果
     */
    public static <T> Result<T> validateFailed(String message) {
        return new Result<>(false, ResultCode.PARAM_ERROR.getCode(), message, null);
    }
    
    /**
     * 未登录返回结果
     * @param <T> 数据类型
     * @return 返回结果
     */
    public static <T> Result<T> unauthorized() {
        return failed(ResultCode.UNAUTHORIZED);
    }
    
    /**
     * 未授权返回结果
     * @param <T> 数据类型
     * @return 返回结果
     */
    public static <T> Result<T> forbidden() {
        return failed(ResultCode.FORBIDDEN);
    }
    
    /**
     * 通用返回失败
     * @param resultCode 结果码
     * @param <T> 数据类型
     * @return 返回结果
     */
    public static <T> Result<T> failure(ResultCode resultCode) {
        return failed(resultCode);
    }
    
    /**
     * 通用返回失败
     * @param resultCode 结果码
     * @param message 提示信息
     * @param <T> 数据类型
     * @return 返回结果
     */
    public static <T> Result<T> failure(ResultCode resultCode, String message) {
        return failed(resultCode, message);
    }
} 