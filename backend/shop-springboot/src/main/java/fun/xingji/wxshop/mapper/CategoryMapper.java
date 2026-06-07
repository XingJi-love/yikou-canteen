package fun.xingji.wxshop.mapper;

import fun.xingji.wxshop.entity.Category;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 分类 Mapper
 * 提供分类的 CRUD 和排序功能
 */
@Mapper
public interface CategoryMapper {

    /** 查询所有分类（按 sort 和 id 升序） */
    @Select("select id, name, sort from wxshop_category order by sort asc, id asc")
    List<Category> selectAll();

    /** 新增分类 */
    @Insert("insert into wxshop_category(name, sort) values(#{name}, #{sort})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Category category);

    /** 更新分类信息 */
    @Update("update wxshop_category set name = #{name}, sort = #{sort} where id = #{id}")
    int update(Category category);

    /** 仅更新排序权重 */
    @Update("update wxshop_category set sort = #{sort} where id = #{id}")
    int updateSort(@Param("id") Integer id, @Param("sort") Integer sort);

    /** 删除分类 */
    @Delete("delete from wxshop_category where id = #{id}")
    int deleteById(Integer id);
}
