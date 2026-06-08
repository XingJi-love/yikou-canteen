package fun.xingji.wxshop.ai.controller;

import fun.xingji.wxshop.ai.dto.AiRequest;
import fun.xingji.wxshop.ai.dto.AiResponse;
import fun.xingji.wxshop.ai.service.AiDataService;
import fun.xingji.wxshop.ai.service.AiService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 管理端 AI 控制器 — 提供 5 个接口：菜品描述、经营简报、套餐推荐、营销参谋、菜品列表。
 * 所有 AI 接口均为同步 JSON 返回。
 */
@RestController
@RequestMapping("/admin/api/ai")
public class AdminAiController {

    private final AiService aiService;
    private final AiDataService aiDataService;

    public AdminAiController(AiService aiService, AiDataService aiDataService) {
        this.aiService = aiService;
        this.aiDataService = aiDataService;
    }

    // ═══════════════════════════════════════════════════════════════
    // 1. AI 菜品描述生成
    // ═══════════════════════════════════════════════════════════════

    /**
     * AI 菜品描述生成 — 港式茶餐厅风格，15-25 字。同步返回 JSON。
     */
    @PostMapping("/food-description")
    public Map<String, Object> foodDescription(@RequestBody AiRequest request) {
        String priceStr = request.getPrice() != null ? request.getPrice().toString() : null;
        AiResponse resp = aiService.generateFoodDescription(
                request.getFoodName(),
                request.getCategory(),
                priceStr
        );
        return Map.of("content", resp.getContent(), "model", resp.getModel());
    }

    // ═══════════════════════════════════════════════════════════════
    // 2. AI 每日经营简报
    // ═══════════════════════════════════════════════════════════════

    /**
     * AI 每日经营简报 — 聚合多维度经营数据，AI 分析趋势+异常+建议。同步返回 JSON。
     */
    @PostMapping("/daily-brief")
    public Map<String, Object> dailyBrief(@RequestBody AiRequest request) {
        String date = request.getDate();
        String data = aiDataService.getDailyBriefData(date);
        AiResponse resp = aiService.generateDailyBrief(data, date);
        return Map.of("content", resp.getContent(), "model", resp.getModel());
    }

    // ═══════════════════════════════════════════════════════════════
    // 3. AI 智能套餐推荐
    // ═══════════════════════════════════════════════════════════════

    /**
     * AI 套餐推荐 — 从 TOP10 高频搭配中推荐 2-3 个套餐。同步返回 JSON。
     */
    @PostMapping("/combo-recommend")
    public Map<String, Object> comboRecommend(@RequestBody AiRequest request) {
        int days = (request.getDays() != null && request.getDays() > 0) ? request.getDays() : 30;
        days = Math.min(days, 365);
        String comboData = aiDataService.getComboAnalysisData(days);
        AiResponse resp = aiService.recommendCombos(comboData);
        return Map.of("content", resp.getContent(), "model", resp.getModel());
    }

    // ═══════════════════════════════════════════════════════════════
    // 4. AI 营销活动参谋
    // ═══════════════════════════════════════════════════════════════

    /**
     * AI 营销参谋 — 结合热销+满减规则生成 2-3 个活动方案。同步返回 JSON。
     */
    @PostMapping("/marketing-advice")
    public Map<String, Object> marketingAdvice(@RequestBody(required = false) AiRequest request) {
        String marketingData = aiDataService.getMarketingData();
        AiResponse resp = aiService.generateMarketingAdvice(marketingData);
        return Map.of("content", resp.getContent(), "model", resp.getModel());
    }

    // ═══════════════════════════════════════════════════════════════
    // 辅助接口
    // ═══════════════════════════════════════════════════════════════

    /**
     * 获取在售菜品列表 — 供前端 el-select 下拉搜索。
     */
    @GetMapping("/foods")
    public Map<String, Object> foodList() {
        return Map.of("data", aiDataService.getFoodsForDescription());
    }
}
