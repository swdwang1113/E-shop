package ptumall.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ptumall.config.JWTInterceptors;
import ptumall.model.UserFavorite;
import ptumall.service.UserFavoriteService;
import ptumall.vo.Result;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/api/favorite")
@Api(tags = "用户收藏接口")
public class UserFavoriteController {
    
    @Autowired
    private UserFavoriteService userFavoriteService;
    
    @PostMapping("/add")
    @ApiOperation("添加收藏")
    public Result<Void> addFavorite(
            HttpServletRequest request,
            @ApiParam(value = "商品ID", required = true) @RequestParam Integer goodsId) {
        Integer userId = (Integer) request.getAttribute(JWTInterceptors.USER_ID_KEY);
        return userFavoriteService.addFavorite(userId, goodsId);
    }
    
    @DeleteMapping("/remove")
    @ApiOperation("取消收藏")
    public Result<Void> removeFavorite(
            HttpServletRequest request,
            @ApiParam(value = "商品ID", required = true) @RequestParam Integer goodsId) {
        Integer userId = (Integer) request.getAttribute(JWTInterceptors.USER_ID_KEY);
        return userFavoriteService.removeFavorite(userId, goodsId);
    }
    
    @GetMapping("/check")
    @ApiOperation("检查是否已收藏")
    public Result<Boolean> checkFavorite(
            HttpServletRequest request,
            @ApiParam(value = "商品ID", required = true) @RequestParam Integer goodsId) {
        Integer userId = (Integer) request.getAttribute(JWTInterceptors.USER_ID_KEY);
        return userFavoriteService.isFavorite(userId, goodsId);
    }
    
    @GetMapping("/list")
    @ApiOperation("获取收藏列表")
    public Result<List<UserFavorite>> getFavoriteList(HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute(JWTInterceptors.USER_ID_KEY);
        return userFavoriteService.getFavoriteList(userId);
    }
} 