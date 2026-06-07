-- ====================================================================
-- 微信商城数据库初始化脚本
-- 包含所有表结构、分类种子数据和示例菜品数据
-- 执行方式: 在 MySQL 客户端中 source wxshop.sql
-- ====================================================================

CREATE DATABASE IF NOT EXISTS `wxshop` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `wxshop`;

-- ----------------------------
-- 已有数据库升级：扩展 wxshop_user 表支持个人信息字段
-- 如果是已有数据库，请单独执行以下 ALTER 语句
-- ----------------------------
-- ALTER TABLE `wxshop_user`
--   ADD COLUMN `nickname` VARCHAR(100) DEFAULT '' NOT NULL COMMENT '用户昵称' AFTER `openid`,
--   ADD COLUMN `avatar_url` VARCHAR(500) DEFAULT '' NOT NULL COMMENT '头像URL' AFTER `nickname`,
--   ADD COLUMN `phone` VARCHAR(20) DEFAULT '' NOT NULL COMMENT '手机号' AFTER `avatar_url`,
--   ADD COLUMN `gender` TINYINT UNSIGNED DEFAULT 0 NOT NULL COMMENT '性别: 0=未知, 1=男, 2=女' AFTER `phone`,
--   ADD COLUMN `update_time` DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间' AFTER `create_time`;

-- ----------------------------
-- 1. 系统配置表 (键值对存储)
-- ----------------------------
CREATE TABLE IF NOT EXISTS `wxshop_setting` (
  `name` VARCHAR(50) PRIMARY KEY,     -- 配置项名称（主键）
  `value` LONGTEXT NOT NULL            -- 配置项值（文本/JSON）
) ENGINE=InnoDB CHARSET=utf8mb4;

INSERT INTO `wxshop_setting` (`name`, `value`) VALUES
-- 微信小程序凭证（请替换为真实的 AppID 和 AppSecret）
('appid', 'wx74bed9598a9938f7'),
('appsecret', '79199915b5e3d5284be1f31d822c6f63'),
-- 满减优惠配置: 满 k 元减 v 元
('promotion', '[{"k":50,"v":10}]'),
-- 首页轮播图 (JSON数组，支持多张)
('img_swiper', '["/static/uploads/banners/1.jpg","/static/uploads/banners/2.jpg","/static/uploads/banners/3.jpg"]'),
-- 今日推荐菜品
('recommend_img', '/static/uploads/foods/15.jpg'),
('recommend_name', '烧味双拼饭'),
('recommend_price', '68'),
-- 店铺信息
('store_name', '一口食堂'),
('store_subtitle', '港式美味 · 用心烹饪'),
('store_description', '正宗港式茶餐厅，传承香港饮食文化。我们坚持使用新鲜食材，为顾客提供地道的港式美食体验。'),
('store_address', '香港九龙旺角弥敦道688号'),
('store_phone', '852-1234-5678'),
('store_hours', '07:00 - 22:00');


-- ----------------------------
-- 2. 管理员表 (MD5 密码)
-- ----------------------------
CREATE TABLE IF NOT EXISTS `wxshop_admin` (
  `id` INT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
  `username` VARCHAR(32) UNIQUE NOT NULL DEFAULT '',     -- 管理员用户名
  `password` VARCHAR(100) NOT NULL DEFAULT ''            -- MD5 加密后的密码
) ENGINE=InnoDB CHARSET=utf8mb4;

-- ----------------------------
-- 3. 微信用户表
-- ----------------------------
CREATE TABLE IF NOT EXISTS `wxshop_user` (
  `id` INT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
  `openid` VARCHAR(255) DEFAULT '' NOT NULL,             -- 微信 OpenID
  `nickname` VARCHAR(100) DEFAULT '' NOT NULL COMMENT '用户昵称',
  `avatar_url` VARCHAR(500) DEFAULT '' NOT NULL COMMENT '头像URL',
  `phone` VARCHAR(20) DEFAULT '' NOT NULL COMMENT '手机号',
  `gender` TINYINT UNSIGNED DEFAULT 0 NOT NULL COMMENT '性别: 0=未知, 1=男, 2=女',
  `price` DECIMAL(10, 2) UNSIGNED NOT NULL DEFAULT 0,    -- 累计消费金额
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB CHARSET=utf8mb4;

-- ----------------------------
-- ----------------------------
CREATE TABLE IF NOT EXISTS `wxshop_category` (
  `id` INT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
  `name` VARCHAR(255) NOT NULL DEFAULT '',  -- 分类名称
  `sort` INT NOT NULL DEFAULT 0             -- 排序号（越小越靠前）
) ENGINE=InnoDB CHARSET=utf8mb4;

INSERT INTO `wxshop_category` (`id`, `name`,`sort`) VALUES
(1, '烧味系列', 1),
(2, '粥粉面饭', 2),
(3, '港式小炒', 3),
(4, '茶餐厅小食', 4),
(5, '饮品甜点', 5),
(6, '套餐系列', 6);

-- ----------------------------
-- 5. 菜品表 (支持软删除)
-- ----------------------------
CREATE TABLE IF NOT EXISTS `wxshop_food` (
  `id` INT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
  `category_id` INT UNSIGNED NOT NULL DEFAULT 0,        -- 所属分类ID
  `name` VARCHAR(255) NOT NULL DEFAULT '',               -- 菜品名称
  `price` DECIMAL(10, 2) UNSIGNED NOT NULL DEFAULT 0,   -- 价格
  `image_url` VARCHAR(255) NOT NULL DEFAULT '',          -- 图片相对路径
  `status` TINYINT UNSIGNED NOT NULL DEFAULT 0,         -- 状态: 0=下架, 1=上架
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME DEFAULT NULL,
  `delete_time` DATETIME DEFAULT NULL                    -- 软删除标记 (非空=已删除)
) ENGINE=InnoDB CHARSET=utf8mb4;

INSERT INTO `wxshop_food` (`id`, `category_id`, `name`, `price`, `image_url`, `status`) VALUES
-- 烧味系列 (categoryId: 1)
(1, 1, '深井烧鹅', 88.00, 'foods/1.jpg', 1),
(2, 1, '蜜汁叉烧', 58.00, 'foods/2.jpg', 1),
(3, 1, '明炉烧鸭', 52.00, 'foods/3.jpg', 1),

-- 粥粉面饭 (categoryId: 2)
(4, 2, '云吞面', 38.00, 'foods/4.jpg', 1),
(5, 2, '干炒牛河', 48.00, 'foods/5.jpg', 1),
(6, 2, '及第粥', 35.00, 'foods/6.jpg', 1),

-- 港式小炒 (categoryId: 3)
(7, 3, '星州炒米粉', 45.00, 'foods/7.jpg', 1),
(8, 3, '椒盐鲜鱿', 68.00, 'foods/8.jpg', 1),

-- 茶餐厅小食 (categoryId: 4)
(9, 4, '菠萝油', 18.00, 'foods/9.jpg', 1),
(10, 4, '西多士', 25.00, 'foods/10.jpg', 1),
(11, 4, '鱼蛋', 15.00, 'foods/11.jpg', 1),

-- 饮品甜点 (categoryId: 5)
(12, 5, '港式奶茶', 20.00, 'foods/12.jpg', 1),
(13, 5, '冻柠茶', 18.00, 'foods/13.jpg', 1),
(14, 5, '杨枝甘露', 28.00, 'foods/14.jpg', 1),

-- 套餐系列 (categoryId: 6)
(15, 6, '烧味双拼饭', 55.00, 'foods/15.jpg', 1),
(16, 6, '常餐A', 42.00, 'foods/16.jpg', 1),

-- 烧味系列（扩充）
(17, 1, '豉油鸡', 56.00, 'foods/17.jpg', 1),
(18, 1, '乳猪拼盘', 128.00, 'foods/18.jpg', 1),
(19, 1, '白切鸡', 62.00, 'foods/19.jpg', 1),
(20, 1, '烧味三宝饭', 65.00, 'foods/20.jpg', 1),
(21, 1, '卤水拼盘', 78.00, 'foods/21.jpg', 1),

-- 粥粉面饭（扩充）
(22, 2, '牛腩捞面', 42.00, 'foods/22.jpg', 1),
(23, 2, '鲜虾肠粉', 28.00, 'foods/23.jpg', 1),
(24, 2, '窝蛋牛肉饭', 46.00, 'foods/24.jpg', 1),
(25, 2, '艇仔粥', 32.00, 'foods/25.jpg', 1),
(26, 2, 'XO酱炒萝卜糕', 36.00, 'foods/26.jpg', 1),
(27, 2, '五香肉丁公仔面', 34.00, 'foods/27.jpg', 1),

-- 港式小炒（扩充）
(28, 3, '干煸四季豆', 42.00, 'foods/28.jpg', 1),
(29, 3, '避风塘炒蟹', 158.00, 'foods/29.jpg', 1),
(30, 3, '咕噜肉', 52.00, 'foods/30.jpg', 1),
(31, 3, '咸蛋黄焗鸡翼', 58.00, 'foods/31.jpg', 1),
(32, 3, '银芽炒肉丝', 38.00, 'foods/32.jpg', 1),

-- 茶餐厅小食（扩充）
(33, 4, '奶油猪仔包', 12.00, 'foods/33.jpg', 1),
(34, 4, '炸两', 16.00, 'foods/34.jpg', 1),
(35, 4, '瑞士鸡翼', 32.00, 'foods/35.jpg', 1),
(36, 4, '煎酿三宝', 28.00, 'foods/36.jpg', 1),
(37, 4, '咖喱牛杂', 36.00, 'foods/37.jpg', 1),
(38, 4, '格子饼', 15.00, 'foods/38.jpg', 1),
(39, 4, '山渣饼', 10.00, 'foods/39.jpg', 1),

-- 饮品甜点（扩充）
(40, 5, '鸳鸯', 22.00, 'foods/40.jpg', 1),
(41, 5, '砵仔糕', 12.00, 'foods/41.jpg', 1),
(42, 5, '红豆冰', 18.00, 'foods/42.jpg', 1),
(43, 5, '杏仁茶', 16.00, 'foods/43.jpg', 1),

-- 套餐系列（扩充）
(44, 6, '常餐B', 45.00, 'foods/44.jpg', 1),
(45, 6, '学生套餐', 38.00, 'foods/45.jpg', 1);

-- ----------------------------
-- 6. 订单表
-- ----------------------------
CREATE TABLE IF NOT EXISTS `wxshop_order` (
  `id` INT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
  `user_id` INT UNSIGNED NOT NULL DEFAULT 0,              -- 下单用户ID
  `price` DECIMAL(10, 2) UNSIGNED NOT NULL DEFAULT 0,     -- 实付金额 (扣减满减后)
  `promotion` DECIMAL(10, 2) UNSIGNED NOT NULL DEFAULT 0, -- 满减优惠金额
  `number` INT UNSIGNED NOT NULL DEFAULT 0,               -- 菜品总数量
  `is_pay` TINYINT UNSIGNED NOT NULL DEFAULT 0,           -- 支付状态: 0=未支付, 1=已支付
  `is_taken` TINYINT UNSIGNED NOT NULL DEFAULT 0,         -- 取餐状态: 0=未取餐, 1=已取餐
  `comment` TEXT NOT NULL,                                 -- 订单备注
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `pay_time` DATETIME DEFAULT NULL,
  `taken_time` DATETIME DEFAULT NULL
) ENGINE=InnoDB CHARSET=utf8mb4;

-- ----------------------------
-- 7. 订单菜品明细表 (多对多关联)
-- ----------------------------
CREATE TABLE IF NOT EXISTS `wxshop_order_food` (
  `id` INT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
  `order_id` INT UNSIGNED NOT NULL DEFAULT 0,            -- 订单ID
  `food_id` INT UNSIGNED NOT NULL DEFAULT 0,             -- 菜品ID
  `number` INT UNSIGNED NOT NULL DEFAULT 0,              -- 购买数量
  `price` DECIMAL(10, 2) NOT NULL DEFAULT 0              -- 下单时菜品单价
) ENGINE=InnoDB CHARSET=utf8mb4;