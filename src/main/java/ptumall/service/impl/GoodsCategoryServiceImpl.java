package ptumall.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ptumall.dao.GoodsCategoryDao;
import ptumall.dao.GoodsDao;
import ptumall.model.Goods;
import ptumall.model.GoodsCategory;
import ptumall.service.GoodsCategoryService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@Service
public class GoodsCategoryServiceImpl implements GoodsCategoryService {

    @Autowired
    private GoodsCategoryDao categoryDao;
    
    @Autowired
    private GoodsDao goodsDao;
    
    @Override
    public GoodsCategory getCategoryById(Integer id) {
        return categoryDao.findById(id);
    }
    
    @Override
    public List<GoodsCategory> getCategoryList(Integer parentId) {
        return categoryDao.findList(parentId);
    }
    
    @Override
    @Transactional
    public GoodsCategory addCategory(GoodsCategory category) {
        // 设置创建时间和更新时间
        Date now = new Date();
        category.setCreateTime(now);
        category.setUpdateTime(now);
        
        // 如果未设置排序值，默认为0
        if (category.getSort() == null) {
            category.setSort(0);
        }
        
        // 如果未设置父分类ID，默认为0（一级分类）
        if (category.getParentId() == null) {
            category.setParentId(0);
            category.setLevel(1);
        } else if (category.getParentId() > 0) {
            // 根据父分类确定level
            GoodsCategory parent = categoryDao.findById(category.getParentId());
            if (parent != null) {
                category.setLevel(parent.getLevel() + 1);
            } else {
                // 父分类不存在，设为一级分类
                category.setParentId(0);
                category.setLevel(1);
            }
        }
        
        int rows = categoryDao.insert(category);
        if (rows > 0) {
            return category;
        }
        return null;
    }
    
    @Override
    @Transactional
    public boolean updateCategory(GoodsCategory category) {
        if (category.getId() == null) {
            return false;
        }
        
        // 设置更新时间
        category.setUpdateTime(new Date());
        
        // 如果修改了父分类，则需要更新level
        if (category.getParentId() != null) {
            if (category.getParentId() == 0) {
                category.setLevel(1);
            } else {
                GoodsCategory parent = categoryDao.findById(category.getParentId());
                if (parent != null) {
                    category.setLevel(parent.getLevel() + 1);
                }
            }
        }
        
        int rows = categoryDao.update(category);
        return rows > 0;
    }
    
    @Override
    @Transactional
    public boolean deleteCategory(Integer id) {
        // 检查是否有子分类
        List<GoodsCategory> children = categoryDao.findList(id);
        if (children != null && !children.isEmpty()) {
            return false; // 有子分类，不能删除
        }
        
        // 检查是否有商品使用此分类
        List<Integer> categoryIds = Arrays.asList(id);
        List<Goods> goodsList = goodsDao.findList(categoryIds, null, null, null);
        if (goodsList != null && !goodsList.isEmpty()) {
            return false; // 有商品使用此分类，不能删除
        }
        
        int rows = categoryDao.deleteById(id);
        return rows > 0;
    }
    
    @Override
    public List<Integer> getCategoryAndChildrenIds(Integer categoryId) {
        List<Integer> categoryIds = new ArrayList<>();
        
        // 如果分类ID为空，直接返回空列表
        if (categoryId == null) {
            return categoryIds;
        }
        
        // 添加当前分类ID
        categoryIds.add(categoryId);
        
        // 递归获取子分类ID
        collectChildCategoryIds(categoryId, categoryIds);
        
        return categoryIds;
    }
    
    /**
     * 递归收集子分类ID
     * @param parentId 父分类ID
     * @param categoryIds 收集的分类ID列表
     */
    private void collectChildCategoryIds(Integer parentId, List<Integer> categoryIds) {
        // 获取直接子分类
        List<GoodsCategory> children = categoryDao.findList(parentId);
        
        if (children != null && !children.isEmpty()) {
            for (GoodsCategory child : children) {
                // 添加子分类ID
                categoryIds.add(child.getId());
                // 递归获取子分类的子分类
                collectChildCategoryIds(child.getId(), categoryIds);
            }
        }
    }
} 