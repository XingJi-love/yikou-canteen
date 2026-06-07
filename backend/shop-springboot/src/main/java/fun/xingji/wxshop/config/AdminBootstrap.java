package fun.xingji.wxshop.config;

import fun.xingji.wxshop.service.AdminAuthService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 管理员初始化配置
 * 在应用启动后自动检查并创建默认管理员账号（admin/123456）
 */
@Configuration
public class AdminBootstrap {

    /**
     * 应用启动后执行：确保默认管理员账号存在
     * CommandLineRunner 在 Spring 容器初始化完成后运行
     *
     * @param adminAuthService 管理员认证服务
     * @return 命令行执行器
     */
    @Bean
    CommandLineRunner initDefaultAdmin(AdminAuthService adminAuthService) {
        return args -> adminAuthService.ensureDefaultAdmin();
    }
}
