package fun.xingji.wxshop.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 订单商品明细实体类（对应数据库表 wxshop_order_food）
 * 记录每个订单中每件商品的 ID、数量、单价
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderFood {
    private Integer id;          // 主键 ID
    private Integer orderId;     // 所属订单 ID
    private Integer foodId;      // 商品 ID
    private Integer number;      // 购买数量
    private BigDecimal price;    // 单价（下单时快照）

    /** 商品名称（关联 wxshop_food 查询时填充，非数据库持久字段） */
    private String foodName;
}
