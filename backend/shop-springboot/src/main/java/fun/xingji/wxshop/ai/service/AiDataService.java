package fun.xingji.wxshop.ai.service;

import fun.xingji.wxshop.service.*;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * AI 数据聚合服务 — 从 Admin*Service 拉取业务数据，拼接为结构化文本，供 AiService 作为 Prompt 上下文。
 */
@Service
public class AiDataService {

    // 通过原有的 Admin*Service 间接访问数据，保证口径一致
    private final AdminStatisticsService statisticsService; // 统计：概览、趋势、热销
    private final AdminFoodService foodService;              // 菜品：列表、搜索
    private final AdminOrderService orderService;            // 订单：明细、状态
    private final AdminSettingService settingService;        // 配置：店铺信息、满减规则

    /**
     * 构造器注入 — 4 个依赖均由 Spring 自动装配
     */
    public AiDataService(AdminStatisticsService statisticsService,
                         AdminFoodService foodService,
                         AdminOrderService orderService,
                         AdminSettingService settingService) {
        this.statisticsService = statisticsService;
        this.foodService = foodService;
        this.orderService = orderService;
        this.settingService = settingService;
    }

    // ═══════════════════════════════════════════════════════════════
    // 菜品列表
    // ═══════════════════════════════════════════════════════════════

    /**
     * 获取在售菜品列表（最多200条），供前端「菜品描述」下拉框使用。
     */
    public List<Map<String, Object>> getFoodsForDescription() {
        // list(name, categoryId, showStopped, page, pageSize)：null=不限、false=只搜在售
        Map<String, Object> result = foodService.list(null, null, false, 1, 200);
        @SuppressWarnings("unchecked") // 已知返回 Map 结构，安全转换
        List<Map<String, Object>> list = (List<Map<String, Object>>) result.get("list");
        return list != null ? list : List.of(); // 防御 null，返回空列表而非 null
    }

    // ═══════════════════════════════════════════════════════════════
    // 每日经营简报 数据聚合
    // ═══════════════════════════════════════════════════════════════

    /**
     * 聚合 5 维经营数据（概览、环比、订单状态、热销TOP5、7天趋势），拼接为结构化文本。
     */
    public String getDailyBriefData(String dateStr) {
        // ---------- 从统计服务拉取各维度数据 ----------
        Map<String, Object> overview = statisticsService.getOverview();       // 经营概览
        Map<String, Object> topFoods = statisticsService.getTopFoods(5, 30); // 热销TOP5
        Map<String, Object> orderTrend = statisticsService.getOrderTrend(7);  // 7天订单趋势
        Map<String, Object> revenue = statisticsService.getRevenue(7);        // 7天营收趋势
        Map<String, Object> orderStatus = statisticsService.getOrderStatus(); // 订单状态分布

        StringBuilder sb = new StringBuilder();
        sb.append("=== 店铺经营数据 ===\n");

        // 指定统计日期（前端可自定义，不传则用"昨天"）
        String displayDate = (dateStr != null && !dateStr.isEmpty()) ? dateStr : "昨天";
        sb.append("统计日期：").append(displayDate).append("\n\n");

        // ── 1. 当前概览 ──
        sb.append("【当前概览】\n");
        sb.append("累计总订单：").append(overview.get("totalOrders")).append(" 单\n");
        sb.append("待取餐订单：").append(overview.get("pendingOrders")).append(" 单\n");
        sb.append("在售商品数：").append(overview.get("totalFoods")).append(" 个\n");
        sb.append("注册用户数：").append(overview.get("totalUsers")).append(" 人\n");
        sb.append("累计总营收：").append(overview.get("totalRevenue")).append(" 元\n\n");

        // ── 2. 今日数据 + 环比 ──
        sb.append("【今日数据】\n");
        sb.append("今日订单：").append(overview.get("todayOrders")).append(" 单\n");
        sb.append("今日营收：").append(overview.get("todayRevenue")).append(" 元\n");
        sb.append("昨日订单：").append(overview.get("yesterdayOrders")).append(" 单\n");
        sb.append("昨日营收：").append(overview.get("yesterdayRevenue")).append(" 元\n");
        // 增长率字段可能是 Integer 或 Double（取决于数据源），统一用 instanceof Number 兼容处理
        Object orderGrowth = overview.get("orderGrowthRate");
        Object revenueGrowth = overview.get("revenueGrowthRate");
        double ogr = orderGrowth instanceof Number ? ((Number) orderGrowth).doubleValue() : 0;
        double rgr = revenueGrowth instanceof Number ? ((Number) revenueGrowth).doubleValue() : 0;
        sb.append(String.format("订单环比：%.1f%%\n", ogr));
        sb.append(String.format("营收环比：%.1f%%\n\n", rgr));

        // ── 3. 订单状态分布 ──
        sb.append("【订单状态分布】\n");
        sb.append("未支付：").append(orderStatus.get("unpaid")).append(" 单\n");
        sb.append("已支付：").append(orderStatus.get("paid")).append(" 单\n");
        sb.append("已取餐：").append(orderStatus.get("taken")).append(" 单\n");
        sb.append("总计：").append(orderStatus.get("total")).append(" 单\n\n");

        // ── 4. 热销 TOP5 ──
        sb.append("【近30天热销TOP5】\n");
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> topList = (List<Map<String, Object>>) topFoods.get("data");
        if (topList != null) {
            int rank = 1;
            for (Map<String, Object> item : topList) {
                sb.append(rank).append(". ").append(item.get("name"))
                        .append(" | 销量：").append(item.get("totalSales"))
                        .append(" | 营收：").append(item.get("revenue")).append("元\n");
                rank++;
            }
        }
        sb.append("\n");

        // ── 5. 近7天订单趋势 ──
        sb.append("【近7天订单趋势】\n");
        @SuppressWarnings("unchecked")
        List<Object> dates = (List<Object>) orderTrend.get("dates");
        @SuppressWarnings("unchecked")
        List<Object> counts = (List<Object>) orderTrend.get("counts");
        // 先输出汇总值，再逐日列出明细
        sb.append("日均订单：").append(orderTrend.get("averageCount")).append(" 单\n");
        sb.append("7天合计：").append(orderTrend.get("totalCount")).append(" 单\n");
        if (dates != null && counts != null) {
            for (int i = 0; i < dates.size(); i++) {
                sb.append("  ").append(dates.get(i)).append(": ").append(counts.get(i)).append("单\n");
            }
        }
        sb.append("\n");

        // ── 6. 近7天营收趋势 ──
        sb.append("【近7天营收趋势】\n");
        @SuppressWarnings("unchecked")
        List<Object> revDates = (List<Object>) revenue.get("dates");
        @SuppressWarnings("unchecked")
        List<Object> revAmounts = (List<Object>) revenue.get("revenues");
        sb.append("日均营收：").append(revenue.get("averageRevenue")).append(" 元\n");
        sb.append("7天合计：").append(revenue.get("totalRevenue")).append(" 元\n");
        if (revDates != null && revAmounts != null) {
            for (int i = 0; i < revDates.size(); i++) {
                sb.append("  ").append(revDates.get(i)).append(": ").append(revAmounts.get(i)).append("元\n");
            }
        }

        return sb.toString();
    }

    // ═══════════════════════════════════════════════════════════════
    // 智能套餐推荐 数据聚合
    // ═══════════════════════════════════════════════════════════════

    /**
     * 分页拉取已支付订单，统计两两菜品搭配频次，输出 TOP10 高频组合。key 按字典序归一化。
     */
    public String getComboAnalysisData(int days) {
        // LinkedHashMap → 按插入顺序遍历；LinkedHashSet → 去重 + 按插入顺序
        Map<String, Integer> pairCount = new LinkedHashMap<>(); // key: "A + B", value: 出现次数
        Set<String> foodNames = new LinkedHashSet<>();          // 去重记录所有菜品名

        // 分页拉取已支付订单，每页 100 条，最多 20 页（共 2000 条，避免接口超时）
        int page = 1;
        int maxPages = 20;       // 最多拉取 20 页
        int pageSize = 100;      // 每页 100 条
        while (page <= maxPages) {
            // orderService.list(搜索词, 状态, 开始时间, 结束时间, 页码, 每页条数)
            // status=1 → 只查已支付订单，排除未支付/已取消
            Map<String, Object> result = orderService.list(null, 1, null, null, page, pageSize);
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> list = (List<Map<String, Object>>) result.get("list");
            if (list == null || list.isEmpty()) break; // 没有更多数据，退出分页循环

            for (Map<String, Object> order : list) {
                // order_food 字段存储该订单的所有菜品明细（List<{name, price, quantity}>）
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> items = (List<Map<String, Object>>) order.get("order_food");
                // 只有 1 个菜品的订单无法产生搭配，跳过
                if (items == null || items.size() < 2) continue;

                // 提取该订单中所有菜品名称（跳过空名）
                List<String> names = new ArrayList<>();
                for (Map<String, Object> item : items) {
                    String name = (String) item.get("name");
                    if (name != null && !name.isEmpty()) {
                        names.add(name);
                        foodNames.add(name);  // 收集所有菜品名，最终输出为"参与分析的全部菜品"
                    }
                }

                // 枚举所有两两组合（i < j 避免重复和自我配对）
                // 如订单含 [A, B, C] → 生成搭配 A+B、A+C、B+C（共 C(3,2)=3 种）
                for (int i = 0; i < names.size(); i++) {
                    for (int j = i + 1; j < names.size(); j++) {
                        String a = names.get(i);
                        String b = names.get(j);
                        // 字典序排序后拼接 key → A+B 和 B+A 归一化为同一种搭配
                        String key = a.compareTo(b) < 0 ? a + " + " + b : b + " + " + a;
                        pairCount.merge(key, 1, Integer::sum); // 存在则 +1，不存在则初始为 1
                    }
                }
            }
            page++;
        }

        // 转换为 List 后按频次降序排列（不修改原 LinkedHashMap）
        List<Map.Entry<String, Integer>> sorted = new ArrayList<>(pairCount.entrySet());
        sorted.sort((a, b) -> b.getValue().compareTo(a.getValue())); // 降序

        // 拼接输出文本 → 传给 AiService 作为 User Prompt
        StringBuilder sb = new StringBuilder();
        sb.append("=== 历史订单菜品搭配分析（近").append(days).append("天已支付订单） ===\n");
        sb.append("共分析订单中包含以下菜品：").append(String.join("、", foodNames)).append("\n\n");
        sb.append("【高频搭配 TOP10】\n");
        int rank = 1;
        for (Map.Entry<String, Integer> entry : sorted) {
            if (rank > 10) break; // 只取前 10 名
            sb.append(rank).append(". ").append(entry.getKey())
                    .append(" → 共同出现 ").append(entry.getValue()).append(" 次\n");
            rank++;
        }
        if (sorted.isEmpty()) {
            sb.append("暂无足够的订单数据进行搭配分析\n");
        }
        return sb.toString();
    }

    // ═══════════════════════════════════════════════════════════════
    // 营销活动参谋 数据聚合
    // ═══════════════════════════════════════════════════════════════

    /**
     * 聚合 4 维营销数据（店铺信息、满减规则、热销TOP10、营收概览），拼接为结构化文本。
     */
    public String getMarketingData() {
        // 拉取 3 个维度的数据
        Map<String, Object> settings = settingService.getSettings();           // 店铺配置：店名、满减规则等
        Map<String, Object> topFoods = statisticsService.getTopFoods(10, 30); // 热销TOP10（近30天）
        Map<String, Object> overview = statisticsService.getOverview();       // 经营概览：累计/今日营收

        StringBuilder sb = new StringBuilder();
        sb.append("=== 店铺营销数据 ===\n\n");

        // ── 1. 店铺信息 ──
        sb.append("【店铺信息】\n");
        sb.append("店名：").append(settings.getOrDefault("store_name", "").toString()).append("\n");
        sb.append("副标题：").append(settings.getOrDefault("store_subtitle", "").toString()).append("\n");
        sb.append("简介：").append(settings.getOrDefault("store_description", "").toString()).append("\n");
        sb.append("地址：").append(settings.getOrDefault("store_address", "").toString()).append("\n");
        sb.append("电话：").append(settings.getOrDefault("store_phone", "").toString()).append("\n");
        sb.append("营业时间：").append(settings.getOrDefault("store_hours", "").toString()).append("\n\n");

        // ── 2. 满减规则 ──
        sb.append("【当前满减规则】\n");
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> promotions = (List<Map<String, Object>>) settings.get("promotion");
        if (promotions != null && !promotions.isEmpty()) {
            for (Map<String, Object> p : promotions) {
                sb.append("满").append(p.get("threshold")).append("元，减").append(p.get("deduction")).append("元\n");
            }
        } else {
            sb.append("未设置满减规则\n");
        }
        sb.append("\n");

        // ── 3. 热销 TOP10 ──
        sb.append("【近30天热销TOP10】\n");
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> topList = (List<Map<String, Object>>) topFoods.get("data");
        if (topList != null) {
            int rank = 1;
            for (Map<String, Object> item : topList) {
                sb.append(rank).append(". ").append(item.get("name"))
                        .append(" | 销量：").append(item.get("totalSales"))
                        .append(" | 营收：").append(item.get("revenue")).append("元\n");
                rank++;
            }
        }
        sb.append("\n");

        // ── 4. 营收概览 ──
        sb.append("【营收概览】\n");
        sb.append("累计营收：").append(overview.get("totalRevenue")).append(" 元\n");
        sb.append("今日营收：").append(overview.get("todayRevenue")).append(" 元\n");
        sb.append("今日订单：").append(overview.get("todayOrders")).append(" 单\n");

        return sb.toString();
    }
}
