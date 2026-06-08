package fun.xingji.wxshop.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * AI 助手统一响应体 — 所有接口返回 {content, model}，content 可能是纯文本或 Markdown。
 */
@Data // Lombok：自动生成 getter/setter
@NoArgsConstructor  // 无参构造（JSON 反序列化需要）
@AllArgsConstructor // 全参构造（代码中快速创建实例）
public class AiResponse {

    /**
     * AI 生成的文本内容（可能是纯文本或 Markdown，前端用 marked 渲染）
     */
    // 来源：ChatClient.call().content() 返回的原始响应文本，不做额外处理
    private String content;

    /**
     * 使用的模型名称（从 application.yml 读取，如 deepseek-v3）
     */
    // 前端用 el-tag 展示，方便区分不同模型效果
    private String model;
}
