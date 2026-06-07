package fun.xingji.wxshop.mapper;

import fun.xingji.wxshop.entity.User;
import org.apache.ibatis.annotations.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * 用户 Mapper
 * 提供用户注册、查询、更新资料、累计消费、分页查询等数据访问方法
 */
@Mapper
public interface UserMapper {

    /** 根据微信 openid 查找用户（用于登录验证） */
    @Select("select id, openid, nickname, avatar_url as avatarUrl, phone, gender, price, create_time as createTime, update_time as updateTime from wxshop_user where openid = #{openid} limit 1")
    User selectByOpenid(String openid);

    /** 根据用户 ID 查询 */
    @Select("select id, openid, nickname, avatar_url as avatarUrl, phone, gender, price, create_time as createTime, update_time as updateTime from wxshop_user where id = #{id} limit 1")
    User selectById(Integer id);

    /** 新增用户（仅插入 openid，其他字段为默认值） */
    @Insert("insert into wxshop_user(openid) values(#{openid})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(User user);

    /** 更新用户个人资料（昵称、头像、手机、性别） */
    @Update("update wxshop_user set nickname = #{nickname}, avatar_url = #{avatarUrl}, phone = #{phone}, gender = #{gender} where id = #{id}")
    int updateProfile(User user);

    /** 按 ID 更新用户全部信息（管理员端使用） */
    @Update("update wxshop_user set nickname = #{nickname}, avatar_url = #{avatarUrl}, phone = #{phone}, gender = #{gender} where id = #{id}")
    int updateById(User user);

    /** 累计消费金额增加（下单支付后调用） */
    @Update("update wxshop_user set price = price + #{amount} where id = #{id}")
    int addPrice(@Param("id") Integer id, @Param("amount") BigDecimal amount);

    /** 用户总数 */
    @Select("select count(*) from wxshop_user")
    int count();

    /** 用户分页列表（按 ID 倒序） */
    @Select("select id, openid, nickname, avatar_url as avatarUrl, phone, gender, price, create_time as createTime, update_time as updateTime from wxshop_user order by id desc limit #{size} offset #{offset}")
    List<User> selectPage(@Param("offset") int offset, @Param("size") int size);
}
