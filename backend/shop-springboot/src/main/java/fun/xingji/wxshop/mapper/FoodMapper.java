package fun.xingji.wxshop.mapper;

import fun.xingji.wxshop.entity.Food;
import org.apache.ibatis.annotations.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 商品 Mapper
 * 小程序端：按分类获取上架商品列表、购物车回显
 * 管理员端：CRUD + 多条件搜索 + 回收站 + 分类分布统计
 * 复杂 SQL 在 XML 中实现，简单 CRUD 使用注解
 */
@Mapper
public interface FoodMapper {

    /** 小程序端：获取所有上架商品（含分类名称，按分类排序） */
    List<Food> selectActiveByCategory();

    /** 商品总数 */
    @Select("select count(*) from wxshop_food")
    int countTotal();

    /** 根据 ID 查询商品 */
    @Select("select id, category_id, name, price, image_url, status, create_time, update_time, delete_time " +
            "from wxshop_food where id = #{id} limit 1")
    Food selectById(Integer id);

    /** 新增商品 */
    @Insert("insert into wxshop_food(category_id, name, price, image_url, status, create_time) " +
            "values(#{categoryId}, #{name}, #{price}, #{imageUrl}, #{status}, #{createTime})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Food food);

    /** 更新商品信息 */
    @Update("update wxshop_food set category_id = #{categoryId}, name = #{name}, price = #{price}, " +
            "image_url = #{imageUrl}, status = #{status}, update_time = #{updateTime} where id = #{id}")
    int update(Food food);

    /** 软删除（设置 delete_time） */
    @Update("update wxshop_food set delete_time = #{deleteTime} where id = #{id} and delete_time is null")
    int softDelete(@Param("id") Integer id, @Param("deleteTime") LocalDateTime deleteTime);

    /** 物理删除 */
    @Delete("delete from wxshop_food where id = #{id}")
    int hardDelete(Integer id);

    /** 查询回收站中商品的图片路径（用于永久删除前清理文件） */
    @Select("select image_url from wxshop_food where id = #{id} and delete_time is not null")
    String selectImageUrlInRecycle(Integer id);

    /** 更新商品图片路径 */
    @Update("update wxshop_food set image_url = #{imageUrl}, update_time = #{updateTime} where id = #{id}")
    int updateImageUrl(@Param("id") Integer id, @Param("imageUrl") String imageUrl,
                       @Param("updateTime") LocalDateTime updateTime);

    /** 插入草稿商品（先上传图片后填写信息） */
    @Insert("insert into wxshop_food(category_id, name, price, image_url, status, create_time, update_time) " +
            "values(0, '', 0, #{imageUrl}, 0, #{createTime}, #{createTime})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertDraft(Food food);

    /** 查询商品的图片路径 */
    @Select("select image_url from wxshop_food where id = #{id}")
    String selectImageUrlById(Integer id);

    /** 清空商品图片路径 */
    @Update("update wxshop_food set image_url = '', update_time = #{updateTime} where id = #{id}")
    int clearImageUrl(@Param("id") Integer id, @Param("updateTime") LocalDateTime updateTime);

    /** 管理员端：商品多条件搜索计数 - XML 动态 SQL */
    int countBySearch(@Param("categoryId") Integer categoryId, @Param("search") String search,
                      @Param("recycle") boolean recycle);

    /** 管理员端：商品多条件搜索分页 - XML 动态 SQL */
    List<Food> selectPageBySearch(@Param("categoryId") Integer categoryId, @Param("search") String search,
                                  @Param("recycle") boolean recycle, @Param("offset") int offset,
                                  @Param("size") int size);

    /** 小程序端：批量查询指定 ID 的商品（购物车回显价格） - XML foreach IN 查询 */
    List<Food> selectByIds(@Param("idList") List<Integer> idList);

    /** 管理员端：各分类下商品数量分布统计 - XML JOIN+GROUP BY+HAVING */
    List<Map<String, Object>> selectCategoryDistribution();
}
