package fun.xingji.wxshop.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 管理员实体类（对应数据库表 wxshop_admin）
 * 用于后台管理系统的登录认证
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Admin {
    private Integer id;        // 主键 ID
    private String username;   // 管理员用户名
    private String password;   // 密码（MD5 加密存储）
}
