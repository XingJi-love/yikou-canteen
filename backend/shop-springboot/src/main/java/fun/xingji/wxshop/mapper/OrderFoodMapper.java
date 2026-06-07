package fun.xingji.wxshop.mapper;

import fun.xingji.wxshop.entity.OrderFood;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * 订单商品明细 Mapper
 * 支持插入订单明细、查询明细列表
 * 复杂 JOIN 查询（含商品名称和图片）在 XML 中实现
 */
@Mapper
public interface OrderFoodMapper {

    /** 插入订单明细记录 */
    @Insert("insert into wxshop_order_food(order_id, food_id, number, price) " +
            "values(#{orderId}, #{foodId}, #{number}, #{price})")
    int insert(OrderFood orderFood);

    /** 根据订单 ID 查询明细（不含商品名称和图片） */
    @Select("select id, order_id, food_id, number, price from wxshop_order_food where order_id = #{orderId}")
    List<OrderFood> selectByOrderId(Integer orderId);

    /** 小程序端：获取订单明细（含商品名称和图片） - XML LEFT JOIN */
    List<Map<String, Object>> selectByOrderIdWithFood(Integer orderId);
}
