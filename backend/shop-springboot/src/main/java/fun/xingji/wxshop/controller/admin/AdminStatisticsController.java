package fun.xingji.wxshop.controller.admin;

import fun.xingji.wxshop.service.AdminStatisticsService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 管理端统计数据 API
 * 提供订单趋势、营收统计、分类分布、热销商品等统计数据
 * 路径前缀: /admin/api/statistics
 */
@RestController
@RequestMapping("/admin/api/statistics")
public class AdminStatisticsController {

    private final AdminStatisticsService statisticsService;

    public AdminStatisticsController(AdminStatisticsService statisticsService) {
        this.statisticsService = statisticsService;
    }

    /**
     * GET /overview - 获取综合统计数据
     * 包括：总订单数、待取餐订单数、商品总数、用户总数、总营收等
     */
    @GetMapping("/overview")
    public Map<String, Object> overview() {
        return statisticsService.getOverview();
    }

    /**
     * GET /order-trend - 获取订单趋势统计
     *
     * @param days 统计天数，默认7天
     */
    @GetMapping("/order-trend")
    public Map<String, Object> orderTrend(@RequestParam(defaultValue = "7") int days) {
        int validDays = Math.min(Math.max(days, 1), 90);
        return statisticsService.getOrderTrend(validDays);
    }

    /**
     * GET /revenue - 获取营收统计
     *
     * @param days 统计天数，默认7天
     */
    @GetMapping("/revenue")
    public Map<String, Object> revenue(@RequestParam(defaultValue = "7") int days) {
        int validDays = Math.min(Math.max(days, 1), 90);
        return statisticsService.getRevenue(validDays);
    }

    /**
     * GET /category-distribution - 获取商品分类分布
     */
    @GetMapping("/category-distribution")
    public Map<String, Object> categoryDistribution() {
        return statisticsService.getCategoryDistribution();
    }

    /**
     * GET /top-foods - 获取热销商品排行
     *
     * @param limit 返回数量，默认10
     * @param days  统计天数，默认30天
     */
    @GetMapping("/top-foods")
    public Map<String, Object> topFoods(
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(defaultValue = "30") int days) {
        int validLimit = Math.min(Math.max(limit, 1), 50);
        int validDays = Math.min(Math.max(days, 1), 365);
        return statisticsService.getTopFoods(validLimit, validDays);
    }

    /**
     * GET /order-status - 获取订单状态分布
     */
    @GetMapping("/order-status")
    public Map<String, Object> orderStatus() {
        return statisticsService.getOrderStatus();
    }
}
