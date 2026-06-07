package fun.xingji.wxshop.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 用户实体类（对应数据库表 wxshop_user）
 * 存储微信小程序用户的 openid、基本信息、累计消费金额
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    private Integer id;              // 主键 ID
    private String openid;           // 微信用户唯一标识
    private String nickname;         // 用户昵称
    private String avatarUrl;        // 用户头像 URL
    private String phone;            // 手机号
    private Integer gender;          // 性别（0=未知，1=男，2=女）
    private BigDecimal price;        // 累计消费金额
    private LocalDateTime createTime; // 注册时间
    private LocalDateTime updateTime; // 最后修改时间
}
