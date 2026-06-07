package fun.xingji.wxshop.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 系统配置实体类（对应数据库表 wxshop_setting）
 * 键值对存储，用于小程序 AppID、满减规则、轮播图、店铺信息等动态配置
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Setting {
    private String name;   // 配置键名
    private String value;  // 配置值（JSON 文本或普通字符串）
}
