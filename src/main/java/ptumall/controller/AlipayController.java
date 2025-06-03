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
 */
@Slf4j
@Api(tags = "支付宝支付接口")
@RestController
@RequestMapping("/api/alipay")
public class AlipayController {

    @Autowired
    private AlipayService alipayService;

    /**
     * 支付宝异步通知接口
     */
    @ApiOperation(value = "支付宝异步通知", notes = "接收支付宝支付结果的异步通知")
    @PostMapping("/notify")
    public String notify(HttpServletRequest request) {
        // 获取支付宝POST过来的信息
        Map<String, String> params = new HashMap<>();
        Map<String, String[]> requestParams = request.getParameterMap();
        
        for (String name : requestParams.keySet()) {
            String[] values = requestParams.get(name);
            String valueStr = "";
            for (int i = 0; i < values.length; i++) {
                valueStr = (i == values.length - 1) ? valueStr + values[i] : valueStr + values[i] + ",";
            }
            params.put(name, valueStr);
        }
        
        log.info("支付宝异步通知，参数：{}", params);
        
        // 处理通知
        boolean result = alipayService.handleNotify(params);
        
        // 返回结果
        return result ? "success" : "fail";
    }
    
    /**
     * 支付宝同步回调接口
     */
    @ApiOperation(value = "支付宝同步回调", notes = "支付成功后跳转回商户页面")
    @GetMapping("/return")
    public void returnUrl(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // 获取订单号
        String outTradeNo = request.getParameter("out_trade_no");
        
        log.info("支付宝同步回调，订单号：{}", outTradeNo);
        
        // 提取原始订单号（去掉用户ID部分）
        String originalOrderNo = outTradeNo;
        if (outTradeNo.contains("_")) {
            originalOrderNo = outTradeNo.split("_")[0];
        }
        
        // 重定向到前端
        response.sendRedirect("http://localhost:5173/payment/result?out_trade_no=" + originalOrderNo);
    }
    
    /**
     * 查询支付状态
     */
    @ApiOperation(value = "查询支付状态", notes = "查询订单支付状态")
    @GetMapping("/query")
    public Result<Boolean> queryPayStatus(@RequestParam String orderNo) {
        boolean isPaid = alipayService.queryPayStatus(orderNo);
        return Result.success(isPaid);
    }
} 