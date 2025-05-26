package ptumall.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ptumall.model.GoodsCategory;
import ptumall.service.GoodsCategoryService;
import ptumall.vo.Result;
import ptumall.vo.ResultCode;

import java.util.List;

@Api(tags = "商品分类接口")
@RestController
@RequestMapping("/category")
public class GoodsCategoryController {

    @Autowired
    private GoodsCategoryService categoryService;
    
    @ApiOperation("获取分类列表")
    @GetMapping("/list")
    public Result<List<GoodsCategory>> getCategoryList(
            @ApiParam(value = "父分类ID", required = false) @RequestParam(required = false) Integer parentId) {
        List<GoodsCategory> categoryList = categoryService.getCategoryList(parentId);
        return Result.success(categoryList);
    }
    
    @ApiOperation("获取分类详情")
    @GetMapping("/{id}")
    public Result<GoodsCategory> getCategoryDetail(
            @ApiParam(value = "分类ID", required = true) @PathVariable Integer id) {
        GoodsCategory category = categoryService.getCategoryById(id);
        if (category != null) {
            return Result.success(category);
        } else {
            return Result.failure(ResultCode.NOT_FOUND, "分类不存在");
        }
    }
} 