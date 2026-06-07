package fun.xingji.wxshop;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 微信点餐系统后端启动入口
 * - @SpringBootApplication：Spring Boot 核心注解
 * - @MapperScan：扫描 Mapper 接口所在包，自动注册 MyBatis Bean
 */
@SpringBootApplication
@MapperScan("fun.xingji.wxshop.mapper")
public class ShopBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(ShopBackendApplication.class, args);
    }
}
