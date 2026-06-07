package fun.xingji.wxshop.mapper;

import fun.xingji.wxshop.entity.Admin;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * 管理员 Mapper
 * 根据用户名查询、修改密码、统计数量、新增管理员
 */
@Mapper
public interface AdminMapper {

    /** 根据用户名查找管理员 */
    @Select("select id, username, password from wxshop_admin where username = #{username} limit 1")
    Admin selectByUsername(String username);

    /** 更新管理员密码 */
    @Update("update wxshop_admin set password = #{password} where id = #{id}")
    int updatePassword(Admin admin);

    /** 管理员总数（用于判断是否需要创建默认账号） */
    @Select("select count(*) from wxshop_admin")
    int count();

    /** 新增管理员 */
    @Insert("insert into wxshop_admin(username, password) values(#{username}, #{password})")
    int insert(Admin admin);
}
