package fun.xingji.wxshop.ai.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * AI 助手统一请求体 — 4 个功能共用，按需读取不同字段。
 * /food-description → foodName/category/price；/daily-brief → date；/combo-recommend → days。
 */
@Data // Lombok：自动生成 getter/setter/toString/equals/hashCode
public class AiRequest {

    /**
     * 菜品描述生成 — 菜品名称（必填）
     */
    // 使用场景：POST /admin/api/ai/food-description | 示例："烧味双拼饭"
    private String foodName;

    /**
     * 菜品描述生成 — 菜品分类（可选，丰富 AI 上下文）
     */
    // 使用场景：POST /admin/api/ai/food-description | 示例："烧腊饭类"
    private String category;

    /**
     * 菜品描述生成 — 菜品价格（可选，BigDecimal 保证金额精度）
     */
    // 使用场景：POST /admin/api/ai/food-description | 示例：68.00
    private BigDecimal price;

    /**
     * 每日经营简报 — 统计日期（可选，默认昨天）
     */
    // 使用场景：POST /admin/api/ai/daily-brief | 格式：yyyy-MM-dd
    private String date;

    /**
     * 智能套餐推荐 — 分析近 N 天订单（可选，默认 30，上限 365）
     */
    // 使用场景：POST /admin/api/ai/combo-recommend | 范围：7 ~ 365
    private Integer days;
}
