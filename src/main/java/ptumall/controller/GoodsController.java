package ptumall.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ptumall.config.JWTInterceptors;
import ptumall.model.Goods;
import ptumall.service.FileService;
import ptumall.service.GoodsService;
import ptumall.utils.AuthUtils;
import ptumall.vo.Result;
import ptumall.vo.ResultCode;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Api(tags = "商品接口")
@RestController
@RequestMapping("/goods")
public class GoodsController {

    @Autowired
    private GoodsService goodsService;
    
    @Autowired
    private AuthUtils authUtils;
    
    @Autowired
    private FileService fileService;
    
    @Value("${file.save-path}")
    private String saveFilePath;
    
    @Value("${image.prefix-url}")
    private String imageUrl;
    
    @ApiOperation("获取商品列表")
    @GetMapping("/list")
    public Result<List<Goods>> getGoodsList(
            @ApiParam(value = "分类ID", required = false) @RequestParam(required = false) Integer categoryId,
            @ApiParam(value = "搜索关键词", required = false) @RequestParam(required = false) String keyword,
            @ApiParam(value = "排序字段", required = false, allowableValues = "price,rating,sales,newest") 
            @RequestParam(required = false) String sortBy,
            @ApiParam(value = "排序方向", required = false, allowableValues = "asc,desc") 
            @RequestParam(required = false, defaultValue = "desc") String sortDirection) {
        List<Goods> goodsList = goodsService.getGoodsList(categoryId, keyword, sortBy, sortDirection);
        return Result.success(goodsList);
    }
    
    @ApiOperation("获取商品详情")
    @GetMapping("/{id}")
    public Result<Goods> getGoodsDetail(
            @ApiParam(value = "商品ID", required = true) @PathVariable Integer id) {
        Goods goods = goodsService.getGoodsById(id);
        if (goods != null) {
            return Result.success(goods);
        } else {
            return Result.failure(ResultCode.NOT_FOUND, "商品不存在");
        }
    }
    
    @ApiOperation("添加商品")
    @PostMapping("/add")
    public Result<Goods> addGoods(
            @RequestBody Goods goods,
            HttpServletRequest request) {
        // 权限校验：只有管理员可以添加商品
        if (!authUtils.isAdmin(request)) {
            return Result.failure(ResultCode.UNAUTHORIZED, "没有权限");
        }
        
        // 参数校验
        if (goods.getName() == null || goods.getName().trim().isEmpty() ||
            goods.getPrice() == null || goods.getCategoryId() == null ||
            goods.getStock() == null || goods.getStock() < 0) {
            return Result.failure(ResultCode.PARAMS_IS_INVALID);
        }
        
        Goods addedGoods = goodsService.addGoods(goods);
        if (addedGoods != null) {
            return Result.success(addedGoods);
        } else {
            return Result.failure(ResultCode.FAILED, "添加商品失败");
        }
    }
    
    @ApiOperation("更新商品")
    @PutMapping("/update")
    public Result<Void> updateGoods(
            @RequestBody Goods goods,
            HttpServletRequest request) {
        // 权限校验：只有管理员可以更新商品
        if (!authUtils.isAdmin(request)) {
            return Result.failure(ResultCode.UNAUTHORIZED, "没有权限");
        }
        
        // 参数校验
        if (goods.getId() == null) {
            return Result.failure(ResultCode.PARAMS_IS_INVALID, "商品ID不能为空");
        }
        
        boolean success = goodsService.updateGoods(goods);
        if (success) {
            return Result.success();
        } else {
            return Result.failure(ResultCode.FAILED, "更新商品失败");
        }
    }
    
    @ApiOperation("删除商品")
    @DeleteMapping("/{id}")
    public Result<Void> deleteGoods(
            @ApiParam(value = "商品ID", required = true) @PathVariable Integer id,
            HttpServletRequest request) {
        // 权限校验：只有管理员可以删除商品
        if (!authUtils.isAdmin(request)) {
            return Result.failure(ResultCode.UNAUTHORIZED, "没有权限");
        }
        
        boolean success = goodsService.deleteGoods(id);
        if (success) {
            return Result.success();
        } else {
            return Result.failure(ResultCode.FAILED, "删除商品失败");
        }
    }
    
    @ApiOperation("上架/下架商品")
    @PutMapping("/status/{id}")
    public Result<Void> updateStatus(
            @ApiParam(value = "商品ID", required = true) @PathVariable Integer id,
            @ApiParam(value = "商品状态", required = true) @RequestParam Byte status,
            HttpServletRequest request) {
        // 权限校验：只有管理员可以上架/下架商品
        if (!authUtils.isAdmin(request)) {
            return Result.failure(ResultCode.UNAUTHORIZED, "没有权限");
        }
        
        // 参数校验
        if (status != 0 && status != 1) {
            return Result.failure(ResultCode.PARAMS_IS_INVALID, "状态值只能是0或1");
        }
        
        boolean success = goodsService.updateStatus(id, status);
        if (success) {
            return Result.success();
        } else {
            return Result.failure(ResultCode.FAILED, "更新商品状态失败");
        }
    }
    
    @ApiOperation("获取推荐商品")
    @GetMapping("/recommend")
    public Result<List<Goods>> getRecommendGoods(
            @ApiParam(value = "限制数量", required = false) @RequestParam(required = false) Integer limit) {
        List<Goods> recommendGoods = goodsService.getRecommendGoods(limit);
        return Result.success(recommendGoods);
    }
    
    @ApiOperation("上传商品图片")
    @PostMapping("/upload/image")
    public Result<String> uploadImage(
            @ApiParam(value = "商品图片", required = true) @RequestParam("file") MultipartFile file,
            HttpServletRequest request) {
        // 权限校验：只有管理员可以上传商品图片
        if (!authUtils.isAdmin(request)) {
            return Result.failure(ResultCode.UNAUTHORIZED, "没有权限");
        }
        
        // 文件为空检查
        if (file.isEmpty()) {
            return Result.failure(ResultCode.PARAMS_IS_INVALID, "上传文件不能为空");
        }
        
        // 使用FileService上传图片
        String imageUrlPath = fileService.uploadImage(file, "goods");
        if (imageUrlPath != null) {
            return Result.success(imageUrlPath);
        } else {
            return Result.failure(ResultCode.FAILED, "图片上传失败");
        }
    }
    
    @ApiOperation("更新商品评分")
    @PutMapping("/{id}/rating")
    public Result<Void> updateRating(
            @ApiParam(value = "商品ID", required = true) @PathVariable Integer id,
            @ApiParam(value = "评分值", required = true) @RequestParam BigDecimal rating,
            HttpServletRequest request) {
        // 权限校验：只有管理员可以更新商品评分
        if (!authUtils.isAdmin(request)) {
            return Result.failure(ResultCode.UNAUTHORIZED, "没有权限");
        }
        
        // 参数校验
        if (rating.compareTo(BigDecimal.ZERO) < 0 || rating.compareTo(new BigDecimal("5")) > 0) {
            return Result.failure(ResultCode.PARAMS_IS_INVALID, "评分值必须在0-5之间");
        }
        
        boolean success = goodsService.updateRating(id, rating);
        if (success) {
            return Result.success();
        } else {
            return Result.failure(ResultCode.FAILED, "更新商品评分失败");
        }
    }
    
    @ApiOperation("更新商品销量")
    @PutMapping("/{id}/sales")
    public Result<Void> updateSalesVolume(
            @ApiParam(value = "商品ID", required = true) @PathVariable Integer id,
            @ApiParam(value = "销量增加值", required = true) @RequestParam Integer increment,
            HttpServletRequest request) {
        // 权限校验：只有管理员可以更新商品销量
        if (!authUtils.isAdmin(request)) {
            return Result.failure(ResultCode.UNAUTHORIZED, "没有权限");
        }
        
        // 参数校验
        if (increment <= 0) {
            return Result.failure(ResultCode.PARAMS_IS_INVALID, "销量增加值必须大于0");
        }
        
        boolean success = goodsService.updateSalesVolume(id, increment);
        if (success) {
            return Result.success();
        } else {
            return Result.failure(ResultCode.FAILED, "更新商品销量失败");
        }
    }
}
