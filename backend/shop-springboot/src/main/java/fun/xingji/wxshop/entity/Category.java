package fun.xingji.wxshop.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 菜品分类实体类（对应数据库表 wxshop_category）
 * 用于菜单左侧分类导航
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Category {
    private Integer id;    // 主键 ID
    private String name;   // 分类名称（如：烧味、小食、饮品）
    private Integer sort;  // 排序权重（升序排列）
}
