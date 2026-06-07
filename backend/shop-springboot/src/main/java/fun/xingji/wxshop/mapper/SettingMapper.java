package fun.xingji.wxshop.mapper;

import fun.xingji.wxshop.entity.Setting;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

/**
 * 系统配置 Mapper
 * 键值对存取，支持 MySQL INSERT ... ON DUPLICATE KEY UPDATE 实现 upsert
 */
@Mapper
public interface SettingMapper {

    /** 根据键名查询配置 */
    @Select("select name, value from wxshop_setting where name = #{name}")
    Setting selectByName(String name);

    /** 插入或更新配置（存在则更新 value，不存在则新增） */
    @Insert("insert into wxshop_setting(name, value) values(#{name}, #{value}) " +
            "on duplicate key update value = #{value}")
    int insertOrUpdate(Setting setting);
}
