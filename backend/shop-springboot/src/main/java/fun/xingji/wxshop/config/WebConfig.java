package fun.xingji.wxshop.config;

import fun.xingji.wxshop.web.AdminInterceptor;
import fun.xingji.wxshop.web.LoginInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Spring MVC 与 Bean 配置
 * 注册拦截器（登录校验）、静态资源映射、RestTemplate Bean
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final LoginInterceptor loginInterceptor;
    private final AdminInterceptor adminInterceptor;

    @Value("${app.static-upload-dir}")
    private String staticUploadDir;

    public WebConfig(LoginInterceptor loginInterceptor,
                     AdminInterceptor adminInterceptor) {
        this.loginInterceptor = loginInterceptor;
        this.adminInterceptor = adminInterceptor;
    }

    /**
     * 注册拦截器：小程序 /api/** 需登录（白名单: checkLogin, login）
     * 管理端 /admin/api/** 需管理员登录（白名单: checkLogin, login）
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(loginInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns("/api/user/checkLogin", "/api/user/login");
        registry.addInterceptor(adminInterceptor)
                .addPathPatterns("/admin/api/**")
                .excludePathPatterns("/admin/api/checkLogin", "/admin/api/login");
    }

    /**
     * 映射静态上传目录到 /static/uploads/** URL
     * 路径由配置文件 app.static-upload-dir 指定
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/static/uploads/**")
                .addResourceLocations("file:" + staticUploadDir + "/");
    }

    /**
     * 提供 RestTemplate Bean，用于调用微信 code2Session API
     */
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
