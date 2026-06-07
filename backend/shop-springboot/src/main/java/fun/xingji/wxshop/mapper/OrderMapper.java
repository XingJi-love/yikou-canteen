package fun.xingji.wxshop.mapper;

import fun.xingji.wxshop.entity.Order;
import org.apache.ibatis.annotations.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 订单 Mapper
 * 小程序端：CRUD + 游标分页 + 消费记录
 * 管理员端：多条件搜索分页 + 标记取餐
 * 统计模块：概览/趋势/营收/状态分布/热销排行（XML 聚合查询）
 */
@Mapper
public interface OrderMapper {

    /** 创建订单 */
    @Insert("insert into wxshop_order(user_id, price, promotion, number, comment, create_time) " +
            "values(#{userId}, #{price}, #{promotion}, #{number}, #{comment}, #{createTime})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Order order);

    /** 根据 ID 查询订单 */
    @Select("select id, user_id, price, promotion, number, is_pay, is_taken, comment, " +
            "create_time, pay_time, taken_time from wxshop_order where id = #{id} limit 1")
    Order selectById(Integer id);

    /** 根据 ID + 用户 ID 查询订单（校验订单归属） */
    @Select("select id, user_id, price, promotion, number, is_pay, is_taken, comment, " +
            "create_time, pay_time, taken_time from wxshop_order where id = #{id} and user_id = #{userId} limit 1")
    Order selectByUserAndId(@Param("id") Integer id, @Param("userId") Integer userId);

    /** 标记支付完成 */
    @Update("update wxshop_order set is_pay = 1, pay_time = #{payTime} where id = #{id}")
    int updatePay(@Param("id") Integer id, @Param("payTime") LocalDateTime payTime);

    /** 更新订单备注（仅未支付时可修改） */
    @Update("update wxshop_order set comment = #{comment} where id = #{id} and user_id = #{userId} and is_pay = 0")
    int updateComment(@Param("id") Integer id, @Param("userId") Integer userId,
                      @Param("comment") String comment);

    /** 设置取餐状态 */
    @Update("update wxshop_order set is_taken = #{isTaken}, taken_time = #{takenTime} where id = #{id}")
    int updateTaken(@Param("id") Integer id, @Param("isTaken") Integer isTaken,
                    @Param("takenTime") LocalDateTime takenTime);

    /** 管理员端：订单多条件搜索计数 */
    int countBySearch(@Param("userId") Integer userId, @Param("isPay") Integer isPay,
                      @Param("isTaken") Integer isTaken, @Param("search") Integer search);

    /** 管理员端：订单多条件搜索分页（含订单明细一对多） */
    List<Order> selectOrdersWithFoods(@Param("userId") Integer userId, @Param("isPay") Integer isPay,
                                      @Param("isTaken") Integer isTaken, @Param("search") Integer search,
                                      @Param("offset") int offset, @Param("size") int size);

    /** 小程序端：用户已支付订单游标分页（含订单明细，消除 N+1） */
    List<Order> selectUserOrdersWithFoods(@Param("userId") Integer userId, @Param("lastId") Integer lastId,
                                          @Param("size") int size);

    /** 概览统计：一次查询返回全部指标 */
    Map<String, Object> selectOverviewStats();

    /** 订单趋势：按日分组 */
    List<Map<String, Object>> selectOrderTrend(@Param("days") int days);

    /** 收入趋势：按日分组 */
    List<Map<String, Object>> selectRevenueTrend(@Param("days") int days);

    /** 订单状态分布 */
    Map<String, Object> selectOrderStatus();

    /** 小程序端：用户已支付订单游标分页 */
    List<Order> selectByUserId(@Param("userId") Integer userId, @Param("lastId") Integer lastId,
                               @Param("size") int size);

    /** 小程序端：用户消费记录 */
    @Select("select id, price, pay_time from wxshop_order where user_id = #{userId} and is_pay = 1 order by id desc")
    List<Map<String, Object>> selectRecordsByUserId(Integer userId);

    /** 热销商品排行（三表 JOIN + 聚合统计） */
    List<Map<String, Object>> selectTopFoods(@Param("days") int days, @Param("limit") int limit);
}
