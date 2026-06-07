package fun.xingji.wxshop.service;

import fun.xingji.wxshop.mapper.FoodMapper;
import fun.xingji.wxshop.mapper.OrderMapper;
import fun.xingji.wxshop.mapper.UserMapper;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 管理端统计服务
 * 概览指标、订单趋势、营收统计、分类分布、热销排行、订单状态分布
 * 所有聚合数据通过 SQL 一次性查询，避免 N+1 循环
 */
@Service
public class AdminStatisticsService extends BaseService {

    private final OrderMapper orderMapper;
    private final FoodMapper foodMapper;
    private final UserMapper userMapper;

    public AdminStatisticsService(OrderMapper orderMapper, FoodMapper foodMapper, UserMapper userMapper) {
        this.orderMapper = orderMapper;
        this.foodMapper = foodMapper;
        this.userMapper = userMapper;
    }

    /** 综合概览：总订单/待取餐/商品/用户数、总营收、今日/昨日数据及增长率 */
    public Map<String, Object> getOverview() {
        Map<String, Object> stats = orderMapper.selectOverviewStats();
        Map<String, Object> result = new HashMap<>();
        Number totalOrders = (Number) stats.get("totalOrders");
        Number pending = (Number) stats.get("pending");
        Number totalRev = (Number) stats.get("totalRevenue");
        Number todayOrders = (Number) stats.get("todayOrders");
        Number todayRev = (Number) stats.get("todayRevenue");
        Number yesterdayOrders = (Number) stats.get("yesterdayOrders");
        Number yesterdayRev = (Number) stats.get("yesterdayRevenue");

        result.put("totalOrders", totalOrders != null ? totalOrders.intValue() : 0);
        result.put("pendingOrders", pending != null ? pending.intValue() : 0);
        result.put("totalFoods", foodMapper.countTotal());
        result.put("totalUsers", userMapper.count());

        BigDecimal totalRevenue = totalRev != null ? new BigDecimal(totalRev.toString()) : BigDecimal.ZERO;
        result.put("totalRevenue", totalRevenue.setScale(2, RoundingMode.HALF_UP));

        int tOrd = todayOrders != null ? todayOrders.intValue() : 0;
        result.put("todayOrders", tOrd);
        BigDecimal todayRevenue = todayRev != null ? new BigDecimal(todayRev.toString()) : BigDecimal.ZERO;
        result.put("todayRevenue", todayRevenue.setScale(2, RoundingMode.HALF_UP));

        int yOrd = yesterdayOrders != null ? yesterdayOrders.intValue() : 0;
        result.put("yesterdayOrders", yOrd);
        BigDecimal yesterdayRevenue = yesterdayRev != null ? new BigDecimal(yesterdayRev.toString()) : BigDecimal.ZERO;
        result.put("yesterdayRevenue", yesterdayRevenue.setScale(2, RoundingMode.HALF_UP));

        double orderGrowthRate = yOrd > 0 ? ((tOrd - yOrd) * 100.0 / yOrd) : 0;
        result.put("orderGrowthRate", BigDecimal.valueOf(orderGrowthRate).setScale(2, RoundingMode.HALF_UP));
        double revenueGrowthRate = yesterdayRevenue.compareTo(BigDecimal.ZERO) > 0 ?
                todayRevenue.subtract(yesterdayRevenue).multiply(BigDecimal.valueOf(100))
                        .divide(yesterdayRevenue, 2, RoundingMode.HALF_UP).doubleValue() : 0;
        result.put("revenueGrowthRate", BigDecimal.valueOf(revenueGrowthRate).setScale(2, RoundingMode.HALF_UP));
        return result;
    }

    /** 订单趋势（按日分组），填充完整日期范围（无数据补 0） */
    public Map<String, Object> getOrderTrend(int days) {
        SimpleDateFormat sdf = new SimpleDateFormat("MM-dd");
        SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd");
        Calendar calendar = Calendar.getInstance();
        Date now = new Date();
        Set<String> allDates = new LinkedHashSet<>();
        for (int i = days - 1; i >= 0; i--) {
            calendar.setTime(now); calendar.add(Calendar.DAY_OF_MONTH, -i);
            allDates.add(sdf.format(calendar.getTime()));
        }
        List<Map<String, Object>> trendData = orderMapper.selectOrderTrend(days);
        Map<String, Integer> trendMap = new LinkedHashMap<>();
        for (Map<String, Object> row : trendData) {
            Object payDateObj = row.get("payDate");
            if (payDateObj == null) continue;
            String dateStr = formatDate(payDateObj, sdf, isoFormat, calendar);
            Number cnt = (Number) row.get("orderCount");
            if (dateStr != null) trendMap.put(dateStr, cnt != null ? cnt.intValue() : 0);
        }
        List<String> dates = new ArrayList<>();
        List<Integer> counts = new ArrayList<>();
        for (String date : allDates) { dates.add(date); counts.add(trendMap.getOrDefault(date, 0)); }
        Map<String, Object> result = new HashMap<>();
        result.put("dates", dates); result.put("counts", counts);
        int totalCount = counts.stream().mapToInt(Integer::intValue).sum();
        result.put("totalCount", totalCount);
        result.put("averageCount", BigDecimal.valueOf(days > 0 ? (double) totalCount / days : 0).setScale(2, RoundingMode.HALF_UP));
        return result;
    }

    /** 营收趋势（按日分组），填充完整日期范围 */
    public Map<String, Object> getRevenue(int days) {
        SimpleDateFormat sdf = new SimpleDateFormat("MM-dd");
        SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd");
        Calendar calendar = Calendar.getInstance();
        Date now = new Date();
        Set<String> allDates = new LinkedHashSet<>();
        for (int i = days - 1; i >= 0; i--) {
            calendar.setTime(now); calendar.add(Calendar.DAY_OF_MONTH, -i);
            allDates.add(sdf.format(calendar.getTime()));
        }
        List<Map<String, Object>> revenueData = orderMapper.selectRevenueTrend(days);
        Map<String, BigDecimal> revenueMap = new LinkedHashMap<>();
        for (Map<String, Object> row : revenueData) {
            Object payDateObj = row.get("payDate");
            if (payDateObj == null) continue;
            String dateStr = formatDate(payDateObj, sdf, isoFormat, calendar);
            Number rev = (Number) row.get("revenue");
            if (dateStr != null) revenueMap.put(dateStr, rev != null ? new BigDecimal(rev.toString()) : BigDecimal.ZERO);
        }
        List<String> dates = new ArrayList<>();
        List<BigDecimal> revenues = new ArrayList<>();
        for (String date : allDates) { dates.add(date); revenues.add(revenueMap.getOrDefault(date, BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP)); }
        Map<String, Object> result = new HashMap<>();
        result.put("dates", dates); result.put("revenues", revenues);
        BigDecimal totalRevenue = revenues.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        result.put("totalRevenue", totalRevenue.setScale(2, RoundingMode.HALF_UP));
        result.put("averageRevenue", days > 0 ? totalRevenue.divide(BigDecimal.valueOf(days), 2, RoundingMode.HALF_UP) : BigDecimal.ZERO);
        return result;
    }

    /** 商品分类分布统计 */
    public Map<String, Object> getCategoryDistribution() {
        List<Map<String, Object>> categories = foodMapper.selectCategoryDistribution();
        int totalFoods = categories.stream().mapToInt(cat -> cat.get("foodCount") instanceof Number ? ((Number) cat.get("foodCount")).intValue() : 0).sum();
        for (Map<String, Object> category : categories) {
            int foodCount = ((Number) category.get("foodCount")).intValue();
            category.put("percentage", BigDecimal.valueOf(totalFoods > 0 ? (foodCount * 100.0 / totalFoods) : 0).setScale(2, RoundingMode.HALF_UP));
        }
        return Map.of("data", categories);
    }

    /** 热销商品排行 */
    public Map<String, Object> getTopFoods(int limit, int days) {
        List<Map<String, Object>> foods = orderMapper.selectTopFoods(days, limit);
        for (Map<String, Object> food : foods) {
            if (food.get("revenue") != null)
                food.put("revenue", new BigDecimal(food.get("revenue").toString()).setScale(2, RoundingMode.HALF_UP));
            if (food.get("price") != null)
                food.put("price", new BigDecimal(food.get("price").toString()).setScale(2, RoundingMode.HALF_UP));
        }
        return Map.of("data", foods);
    }

    /** 订单状态分布（未支付/已支付/已取餐/总计） */
    public Map<String, Object> getOrderStatus() {
        Map<String, Object> status = orderMapper.selectOrderStatus();
        Map<String, Object> result = new HashMap<>();
        result.put("unpaid", toInt(status.get("unpaid")));
        result.put("paid", toInt(status.get("paid")));
        result.put("taken", toInt(status.get("taken")));
        result.put("total", toInt(status.get("total")));
        return result;
    }

    private int toInt(Object v) { return v instanceof Number ? ((Number) v).intValue() : 0; }

    private String formatDate(Object payDateObj, SimpleDateFormat sdf, SimpleDateFormat isoFormat, Calendar calendar) {
        if (payDateObj instanceof java.sql.Date || payDateObj instanceof java.util.Date) {
            calendar.setTime((java.util.Date) payDateObj);
            return sdf.format(calendar.getTime());
        }
        try {
            calendar.setTime(isoFormat.parse(payDateObj.toString()));
            return sdf.format(calendar.getTime());
        } catch (Exception e) { return null; }
    }
}
