package ptumall.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ptumall.model.GoodsCategory;
import ptumall.service.GoodsCategoryService;
import ptumall.utils.AuthUtils;
import ptumall.vo.Result;
import ptumall.vo.ResultCode;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Api(tags = "管理员-商品分类接口")
@RestController
@RequestMapping("/api/admin/categories")
public class AdminCategoryController {

    @Autowired
    private GoodsCategoryService categoryService;
    
    @Autowired
    private AuthUtils authUtils;
    
    @ApiOperation("获取分类列表")
    @GetMapping("")
    public Result<List<GoodsCategory>> getCategoryList(
            @ApiParam(value = "父分类ID", required = false) @RequestParam(required = false) Integer parentId,
            HttpServletRequest request) {
        // 权限校验：只有管理员可以访问
        if (!authUtils.isAdmin(request)) {
            return Result.failure(ResultCode.UNAUTHORIZED, "没有权限");
        }
        
        List<GoodsCategory> categoryList = categoryService.getCategoryList(parentId);
        return Result.success(categoryList);
    }
    
    @ApiOperation("获取分类详情")
    @GetMapping("/{id}")
    public Result<GoodsCategory> getCategoryDetail(
            @ApiParam(value = "分类ID", required = true) @PathVariable Integer id,
            HttpServletRequest request) {
        // 权限校验：只有管理员可以访问
        if (!authUtils.isAdmin(request)) {
            return Result.failure(ResultCode.UNAUTHORIZED, "没有权限");
        }
        
        GoodsCategory category = categoryService.getCategoryById(id);
        if (category != null) {
            return Result.success(category);
        } else {
            return Result.failure(ResultCode.NOT_FOUND, "分类不存在");
        }
    }
    
    @ApiOperation("添加分类")
    @PostMapping("")
    public Result<GoodsCategory> addCategory(
            @RequestBody GoodsCategory category,
            HttpServletRequest request) {
        // 权限校验：只有管理员可以添加分类
        if (!authUtils.isAdmin(request)) {
            return Result.failure(ResultCode.UNAUTHORIZED, "没有权限");
        }
        
        // 参数校验
        if (category.getName() == null || category.getName().trim().isEmpty()) {
            return Result.failure(ResultCode.PARAMS_IS_INVALID, "分类名称不能为空");
        }
        
        GoodsCategory addedCategory = categoryService.addCategory(category);
        if (addedCategory != null) {
            return Result.success(addedCategory);
        } else {
            return Result.failure(ResultCode.FAILED, "添加分类失败");
        }
    }
    
    @ApiOperation("更新分类")
    @PutMapping("/{id}")
    public Result<Void> updateCategory(
            @ApiParam(value = "分类ID", required = true) @PathVariable Integer id,
            @RequestBody GoodsCategory category,
            HttpServletRequest request) {
        // 权限校验：只有管理员可以更新分类
        if (!authUtils.isAdmin(request)) {
            return Result.failure(ResultCode.UNAUTHORIZED, "没有权限");
        }
        
        // 设置ID
        category.setId(id);
        
        // 参数校验
        if (category.getName() == null || category.getName().trim().isEmpty()) {
            return Result.failure(ResultCode.PARAMS_IS_INVALID, "分类名称不能为空");
        }
        
        boolean success = categoryService.updateCategory(category);
        if (success) {
            return Result.success();
        } else {
            return Result.failure(ResultCode.FAILED, "更新分类失败");
        }
    }
    
    @ApiOperation("删除分类")
    @DeleteMapping("/{id}")
    public Result<Void> deleteCategory(
            @ApiParam(value = "分类ID", required = true) @PathVariable Integer id,
            HttpServletRequest request) {
        // 权限校验：只有管理员可以删除分类
        if (!authUtils.isAdmin(request)) {
            return Result.failure(ResultCode.UNAUTHORIZED, "没有权限");
        }
        
        boolean success = categoryService.deleteCategory(id);
        if (success) {
            return Result.success();
        } else {
            return Result.failure(ResultCode.FAILED, "删除分类失败，请确保分类下没有子分类或商品");
        }
    }
} 