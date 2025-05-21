package ptumall.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ptumall.config.JWTInterceptors;
import ptumall.model.Cart;
import ptumall.service.CartService;
import ptumall.vo.Result;
import ptumall.vo.ResultCode;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Api(tags = "购物车接口")
@RestController
@RequestMapping("/cart")
public class CartController {

    @Autowired
    private CartService cartService;
    
    @ApiOperation("获取购物车列表")
    @GetMapping("/list")
    public Result<List<Cart>> getCartList(HttpServletRequest request) {
        // 从请求中获取用户ID
        Integer userId = (Integer) request.getAttribute(JWTInterceptors.USER_ID_KEY);
        if (userId == null) {
            return Result.failure(ResultCode.UNAUTHORIZED, "请先登录");
        }
        
        List<Cart> cartList = cartService.getCartList(userId);
        return Result.success(cartList);
    }
    
    @ApiOperation("添加商品到购物车")
    @PostMapping("/add")
    public Result<Void> addToCart(
            @ApiParam(value = "商品ID", required = true) @RequestParam Integer goodsId,
            @ApiParam(value = "数量", required = true) @RequestParam Integer quantity,
            HttpServletRequest request) {
        // 从请求中获取用户ID
        Integer userId = (Integer) request.getAttribute(JWTInterceptors.USER_ID_KEY);
        if (userId == null) {
            return Result.failure(ResultCode.UNAUTHORIZED, "请先登录");
        }
        
        // 参数校验
        if (goodsId == null || quantity == null || quantity <= 0) {
            return Result.failure(ResultCode.PARAMS_IS_INVALID, "参数无效");
        }
        
        boolean success = cartService.addToCart(userId, goodsId, quantity);
        if (success) {
            return Result.success();
        } else {
            return Result.failure(ResultCode.FAILED, "添加失败，可能商品已下架或库存不足");
        }
    }
    
    @ApiOperation("更新购物车商品数量")
    @PutMapping("/update")
    public Result<Void> updateQuantity(
            @ApiParam(value = "购物车ID", required = true) @RequestParam Integer cartId,
            @ApiParam(value = "新数量", required = true) @RequestParam Integer quantity,
            HttpServletRequest request) {
        // 从请求中获取用户ID
        Integer userId = (Integer) request.getAttribute(JWTInterceptors.USER_ID_KEY);
        if (userId == null) {
            return Result.failure(ResultCode.UNAUTHORIZED, "请先登录");
        }
        
        // 参数校验
        if (cartId == null || quantity == null) {
            return Result.failure(ResultCode.PARAMS_IS_INVALID, "参数无效");
        }
        
        boolean success = cartService.updateQuantity(userId, cartId, quantity);
        if (success) {
            return Result.success();
        } else {
            return Result.failure(ResultCode.FAILED, "更新失败，可能商品已下架或库存不足");
        }
    }
    
    @ApiOperation("删除购物车商品")
    @DeleteMapping("/delete/{cartId}")
    public Result<Void> deleteCartItem(
            @ApiParam(value = "购物车ID", required = true) @PathVariable Integer cartId,
            HttpServletRequest request) {
        // 从请求中获取用户ID
        Integer userId = (Integer) request.getAttribute(JWTInterceptors.USER_ID_KEY);
        if (userId == null) {
            return Result.failure(ResultCode.UNAUTHORIZED, "请先登录");
        }
        
        boolean success = cartService.deleteCartItem(userId, cartId);
        if (success) {
            return Result.success();
        } else {
            return Result.failure(ResultCode.FAILED, "删除失败");
        }
    }
    
    @ApiOperation("删除购物车中的商品（按商品ID）")
    @DeleteMapping("/delete/goods/{goodsId}")
    public Result<Void> deleteCartItemByGoodsId(
            @ApiParam(value = "商品ID", required = true) @PathVariable Integer goodsId,
            HttpServletRequest request) {
        // 从请求中获取用户ID
        Integer userId = (Integer) request.getAttribute(JWTInterceptors.USER_ID_KEY);
        if (userId == null) {
            return Result.failure(ResultCode.UNAUTHORIZED, "请先登录");
        }
        
        boolean success = cartService.deleteCartItemByGoodsId(userId, goodsId);
        if (success) {
            return Result.success();
        } else {
            return Result.failure(ResultCode.FAILED, "删除失败");
        }
    }
    
    @ApiOperation("清空购物车")
    @DeleteMapping("/clear")
    public Result<Void> clearCart(HttpServletRequest request) {
        // 从请求中获取用户ID
        Integer userId = (Integer) request.getAttribute(JWTInterceptors.USER_ID_KEY);
        if (userId == null) {
            return Result.failure(ResultCode.UNAUTHORIZED, "请先登录");
        }
        
        boolean success = cartService.clearCart(userId);
        if (success) {
            return Result.success();
        } else {
            return Result.failure(ResultCode.FAILED, "清空购物车失败");
        }
    }
}
