package fun.xingji.wxshop.ai.config;

import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * AI 配置类 — 注册 ChatClient Bean。
 * 调用链路：ChatClient → DashScopeChatModel → 阿里云百炼 API。
 */
@Configuration
public class AiConfig {

    /**
     * 注册 ChatClient Bean — Fluent API 链式调用入口。chatModel 由 starter 自动配置。
     */
    @Bean // 向 Spring 容器注册为 Bean，其他组件可通过构造器注入获取
    public ChatClient chatClient(DashScopeChatModel chatModel) {
        // 使用 Builder 模式创建 ChatClient 实例
        // .builder(chatModel)：绑定底层模型（自动注入的 DashScopeChatModel）
        // .build()：构建不可变的 ChatClient 对象（线程安全）
        return ChatClient.builder(chatModel).build();
    }
}
