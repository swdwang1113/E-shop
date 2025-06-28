package ptumall.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ptumall.service.AlipayService;
import ptumall.vo.Result;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * 支付宝回调控制器
 * 处理支付宝支付过程中的各种回调和查询请求
 * 包括：异步通知、同步回调和支付状态查询
 */
@Slf4j 
@Api(tags = "支付宝支付接口")  // Swagger注解，定义API文档分组
@RestController  // Spring MVC注解，标识这是一个REST风格的控制器
@RequestMapping("/api/alipay")  // 定义控制器的基础URL路径
public class AlipayController {

    @Autowired
    private AlipayService alipayService;

    /**
     * 支付宝异步通知接口
     * 接收支付宝服务器发送的支付结果通知
     * 异步通知是支付宝确保商户系统接收到支付结果的机制
     * 即使用户支付后关闭了页面，支付宝仍会通过此接口通知支付结果
     * 
     * @param request HTTP请求对象，包含支付宝发送的所有参数
     * @return 处理结果，返回"success"表示成功接收，返回"fail"表示处理失败，支付宝会重新发送通知
     */
    @ApiOperation(value = "支付宝异步通知", notes = "接收支付宝支付结果的异步通知")
    @PostMapping("/notify")  // 映射POST请求到/api/alipay/notify路径
    public String notify(HttpServletRequest request) {
        // 获取支付宝POST过来的信息
        // 将请求参数转换为Map格式，方便处理
        Map<String, String> params = new HashMap<>();
        Map<String, String[]> requestParams = request.getParameterMap();
        
        // 遍历请求参数，将数组格式的值转换为字符串
        for (String name : requestParams.keySet()) {
            String[] values = requestParams.get(name);
            String valueStr = "";
            for (int i = 0; i < values.length; i++) {
                valueStr = (i == values.length - 1) ? valueStr + values[i] : valueStr + values[i] + ",";
            }
            params.put(name, valueStr);
        }
        
        log.info("支付宝异步通知，参数：{}", params);
        
        // 调用服务层处理通知，验证签名并更新订单状态
        boolean result = alipayService.handleNotify(params);
        
        // 返回处理结果给支付宝
        // 返回"success"字符串告诉支付宝通知已成功处理，不要再发送通知
        // 返回"fail"字符串则支付宝会重新发送通知
        return result ? "success" : "fail";
    }
    
    /**
     * 支付宝同步回调接口
     * 用户在支付宝完成支付后，支付宝会跳转回商户网站的此接口
     * 主要用于改善用户体验，不能作为支付成功的依据
     * 真正的支付结果应以异步通知接口的结果为准
     * 
     * @param request HTTP请求对象，包含支付宝返回的参数
     * @param response HTTP响应对象，用于重定向到前端页面
     * @throws IOException 如果重定向过程中发生IO异常
     */
    @ApiOperation(value = "支付宝同步回调", notes = "支付成功后跳转回商户页面")
    @GetMapping("/return")  // 映射GET请求到/api/alipay/return路径
    public void returnUrl(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // 获取订单号参数
        String outTradeNo = request.getParameter("out_trade_no");
        
        log.info("支付宝同步回调，订单号：{}", outTradeNo);
        
        // 提取原始订单号（去掉用户ID部分）
        // 假设订单号格式为：原始订单号_用户ID
        String originalOrderNo = outTradeNo;
        if (outTradeNo.contains("_")) {
            originalOrderNo = outTradeNo.split("_")[0];
        }
        
        // 重定向到前端支付结果页面
        // 将原始订单号作为参数传递，前端可以根据订单号查询详细支付结果
        response.sendRedirect("http://localhost:5173/payment/result?out_trade_no=" + originalOrderNo);
    }
    
    /**
     * 查询支付状态接口
     * 用于前端主动查询订单的支付状态
     * 可以在用户支付后或者在支付结果页面调用此接口确认支付结果
     * 
     * @param orderNo 订单编号
     * @return 包装了支付状态的结果对象，true表示已支付，false表示未支付
     */
    @ApiOperation(value = "查询支付状态", notes = "查询订单支付状态")
    @GetMapping("/query")  // 映射GET请求到/api/alipay/query路径
    public Result<Boolean> queryPayStatus(@RequestParam String orderNo) {
        // 调用服务层查询支付状态
        boolean isPaid = alipayService.queryPayStatus(orderNo);
        // 返回查询结果
        return Result.success(isPaid);
    }
} 