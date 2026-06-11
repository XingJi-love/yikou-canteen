package fun.xingji.wxshop.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 商品实体类（对应数据库表 wxshop_food）
 * 含分类 ID、名称、价格、图片、上下架状态，支持逻辑删除
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Food {
    private Integer id;              // 主键 ID
    private Integer categoryId;      // 所属分类 ID
    private String name;             // 商品名称
    private BigDecimal price;        // 单价
    private String imageUrl;         // 商品图片路径
    private Integer status;          // 状态（1=上架，0=下架）
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private LocalDateTime deleteTime; // 逻辑删除时间（非 null 表示已删除）

    /** 分类名称（关联分类表查询时填充，非数据库持久字段） */
    private String categoryName;

    /** 菜品详细描述（支持多行文本） */
    private String description;
}
