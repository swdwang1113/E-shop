package ptumall.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ptumall.model.Goods;
import ptumall.service.GoodsService;
import ptumall.vo.Result;
import ptumall.vo.ResultCode;

import java.util.List;

@Api(tags = "商品接口")
@RestController
@RequestMapping("/goods")
public class GoodsController {

    @Autowired
    private GoodsService goodsService;
    
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
    
    @ApiOperation("获取推荐商品")
    @GetMapping("/recommend")
    public Result<List<Goods>> getRecommendGoods(
            @ApiParam(value = "限制数量", required = false) @RequestParam(required = false) Integer limit) {
        List<Goods> recommendGoods = goodsService.getRecommendGoods(limit);
        return Result.success(recommendGoods);
    }
}
