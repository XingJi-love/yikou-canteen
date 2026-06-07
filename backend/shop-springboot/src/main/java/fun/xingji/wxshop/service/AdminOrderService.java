package fun.xingji.wxshop.service;

import fun.xingji.wxshop.entity.Order;
import fun.xingji.wxshop.entity.OrderFood;
import fun.xingji.wxshop.exception.ApiException;
import fun.xingji.wxshop.mapper.OrderMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.*;

/**
 * 管理端订单服务
 * 多条件搜索分页（支付/取餐状态、用户ID、取餐码/订单ID）
 * 标记/取消取餐
 */
@Service
public class AdminOrderService extends BaseService {

    private final OrderMapper orderMapper;

    public AdminOrderService(OrderMapper orderMapper) {
        this.orderMapper = orderMapper;
    }

    /**
     * 分页查询订单列表（多条件筛选），含订单食材明细（一次 JOIN 消除 N+1）
     */
    public Map<String, Object> list(Integer userId, Integer isPay, Integer isTaken, String search, int page, int pageSize) {
        int currentPage = Math.max(page, 1);
        int size = Math.min(Math.max(pageSize, 1), 100);
        int offset = (currentPage - 1) * size;
        Integer searchId = null;
        if (StringUtils.hasText(search)) {
            String trimmed = search.trim();
            if (trimmed.startsWith("A") || trimmed.startsWith("a")) trimmed = trimmed.substring(1);
            searchId = parseInt(trimmed);
        }
        int total = orderMapper.countBySearch(userId, isPay, isTaken, searchId);
        List<Order> orders = orderMapper.selectOrdersWithFoods(userId, isPay, isTaken, searchId, offset, size);
        List<Map<String, Object>> list = new ArrayList<>();
        for (Order order : orders) {
            Map<String, Object> map = new LinkedHashMap<>();
            Integer oid = order.getId();
            map.put("id", oid);
            map.put("user_id", order.getUserId());
            map.put("price", order.getPrice());
            map.put("promotion", order.getPromotion());
            map.put("number", order.getNumber());
            map.put("is_pay", order.getIsPay() == 1);
            map.put("is_taken", order.getIsTaken() == 1);
            map.put("comment", order.getComment());
            map.put("create_time", order.getCreateTime());
            map.put("pay_time", order.getPayTime());
            map.put("taken_time", order.getTakenTime());
            map.put("code", "A" + String.format("%02d", oid));
            map.put("sn", "WX" + String.format("%014d", oid));
            List<Map<String, Object>> items = new ArrayList<>();
            if (order.getOrderFoods() != null) {
                for (OrderFood of : order.getOrderFoods()) {
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("food_id", of.getFoodId());
                    row.put("number", of.getNumber());
                    row.put("price", of.getPrice());
                    row.put("name", of.getFoodName());
                    items.add(row);
                }
            }
            map.put("order_food", items);
            list.add(map);
        }
        Map<String, Object> res = new HashMap<>();
        res.put("list", list);
        res.put("total", total);
        res.put("page", currentPage);
        res.put("pageSize", size);
        return res;
    }

    /**
     * 设置订单取餐/取消取餐状态
     */
    public Map<String, Object> setTaken(Integer id, boolean taken) {
        if (id == null || id <= 0) throw new ApiException("订单不存在");
        if (orderMapper.updateTaken(id, taken ? 1 : 0, LocalDateTime.now()) <= 0)
            throw new ApiException("订单不存在");
        return Map.of("msg", (taken ? "标记取餐" : "取消取餐") + "成功");
    }

    private Integer parseInt(String value) {
        try {
            return Integer.parseInt(value);
        } catch (Exception e) {
            return -1;
        }
    }
}
