package ptumall.service;

import ptumall.model.GoodsCategory;
import java.util.List;

public interface GoodsCategoryService {
    /**
     * 根据ID查询分类
     * @param id 分类ID
     * @return 分类对象
     */
    GoodsCategory getCategoryById(Integer id);
    
    /**
     * 查询分类列表
     * @param parentId 父分类ID，可为null
     * @return 分类列表
     */
    List<GoodsCategory> getCategoryList(Integer parentId);
    
    /**
     * 添加分类
     * @param category 分类对象
     * @return 添加后的分类对象（包含ID）
     */
    GoodsCategory addCategory(GoodsCategory category);
    
    /**
     * 更新分类
     * @param category 分类对象
     * @return 是否成功
     */
    boolean updateCategory(GoodsCategory category);
    
    /**
     * 删除分类
     * @param id 分类ID
     * @return 是否成功
     */
    boolean deleteCategory(Integer id);
    
    /**
     * 获取指定分类及其所有子分类的ID列表
     * @param categoryId 分类ID
     * @return 分类ID列表（包含指定分类ID及其所有子分类ID）
     */
    List<Integer> getCategoryAndChildrenIds(Integer categoryId);
} 