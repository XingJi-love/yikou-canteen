package fun.xingji.wxshop.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 订单实体类（对应数据库表 wxshop_order）
 * 包含用户 ID、实付金额、满减优惠、件数、支付/取餐状态、时间线
 * 一对多关联 OrderFood 订单商品明细
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Order {
    private Integer id;              // 主键 ID
    private Integer userId;          // 下单用户 ID
    private BigDecimal price;        // 实付金额（已减满减）
    private BigDecimal promotion;    // 满减优惠金额
    private Integer number;          // 商品总件数
    private Integer isPay;           // 是否已支付（0=未付，1=已付）
    private Integer isTaken;         // 是否已取餐（0=未取，1=已取）
    private String comment;          // 订单备注
    private LocalDateTime createTime;
    private LocalDateTime payTime;   // 支付完成时间
    private LocalDateTime takenTime; // 取餐时间

    /** 订单商品明细（一对多关联，通过 MyBatis collection 映射） */
    private List<OrderFood> orderFoods;
}
