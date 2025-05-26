package ptumall.dao;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import ptumall.model.GoodsCategory;

import java.util.List;

@Mapper
public interface GoodsCategoryDao {
    /**
     * 通过ID查询分类
     * @param id 分类ID
     * @return 分类对象
     */
    GoodsCategory findById(Integer id);
    
    /**
     * 查询分类列表
     * @param parentId 父分类ID，可为null
     * @return 分类列表
     */
    List<GoodsCategory> findList(@Param("parentId") Integer parentId);
    
    /**
     * 添加分类
     * @param category 分类对象
     * @return 影响的行数
     */
    int insert(GoodsCategory category);
    
    /**
     * 更新分类
     * @param category 分类对象
     * @return 影响的行数
     */
    int update(GoodsCategory category);
    
    /**
     * 删除分类
     * @param id 分类ID
     * @return 影响的行数
     */
    int deleteById(Integer id);
} 