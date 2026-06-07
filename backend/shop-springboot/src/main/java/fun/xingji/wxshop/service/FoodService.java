package fun.xingji.wxshop.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import fun.xingji.wxshop.entity.Food;
import fun.xingji.wxshop.entity.Order;
import fun.xingji.wxshop.entity.OrderFood;
import fun.xingji.wxshop.exception.ApiException;
import fun.xingji.wxshop.mapper.FoodMapper;
import fun.xingji.wxshop.mapper.OrderFoodMapper;
import fun.xingji.wxshop.mapper.OrderMapper;
import fun.xingji.wxshop.mapper.UserMapper;
import fun.xingji.wxshop.util.CommonUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;

/**
 * 小程序端商品与订单服务
 * 处理：首页数据组装、菜单列表、下单（含满减计算）、支付、订单查询、消费记录
 */
@Service
public class FoodService extends BaseService {

    private final FoodMapper foodMapper;
    private final OrderMapper orderMapper;
    private final OrderFoodMapper orderFoodMapper;
    private final UserMapper userMapper;
    private final SettingService settingService;
    private final ObjectMapper objectMapper;

    public FoodService(FoodMapper foodMapper, OrderMapper orderMapper,
                       OrderFoodMapper orderFoodMapper, UserMapper userMapper,
                       SettingService settingService, ObjectMapper objectMapper) {
        this.foodMapper = foodMapper;
        this.orderMapper = orderMapper;
        this.orderFoodMapper = orderFoodMapper;
        this.userMapper = userMapper;
        this.settingService = settingService;
        this.objectMapper = objectMapper;
    }

    /**
     * 首页数据：轮播图、今日推荐、店铺信息（全部从系统配置读取，有默认值兜底）
     */
    public Map<String, Object> index(String domain) {
        List<String> imgSwiper = readJsonList(settingService.get("img_swiper"));
        if (imgSwiper == null || imgSwiper.isEmpty()) {
            imgSwiper = Arrays.asList("/static/uploads/banners/1.jpg", "/static/uploads/banners/2.jpg", "/static/uploads/banners/3.jpg");
        }
        List<String> imgCategory = readJsonList(settingService.get("img_category"));
        String imgAd = CommonUtil.urlFix(domain, settingService.get("img_ad"));

        List<String> swiperUrl = imgSwiper.stream().map(v -> CommonUtil.urlFix(domain, v)).toList();
        List<String> categoryUrl = imgCategory.stream().map(v -> CommonUtil.urlFix(domain, v)).toList();

        String recommendImg = CommonUtil.urlFix(domain, settingService.get("recommend_img"));
        String recommendName = settingService.get("recommend_name");
        String recommendPrice = settingService.get("recommend_price");

        String storeName = settingService.get("store_name");
        String storeSubtitle = settingService.get("store_subtitle");
        String storeDescription = settingService.get("store_description");
        String storeAddress = settingService.get("store_address");
        String storePhone = settingService.get("store_phone");
        String storeHours = settingService.get("store_hours");

        Map<String, Object> res = new HashMap<>();
        res.put("img_swiper", swiperUrl);
        res.put("img_ad", imgAd);
        res.put("img_category", categoryUrl);
        res.put("recommend_img", recommendImg);
        res.put("recommend_name", recommendName != null ? recommendName : "烧味双拼饭");
        res.put("recommend_price", recommendPrice != null ? recommendPrice : "68");
        res.put("store_name", storeName != null ? storeName : "一口食堂");
        res.put("store_subtitle", storeSubtitle != null ? storeSubtitle : "港式美味 · 用心烹饪");
        res.put("store_description", storeDescription != null ? storeDescription : "正宗港式茶餐厅，传承香港饮食文化。我们坚持使用新鲜食材，为顾客提供地道的港式美食体验。");
        res.put("store_address", storeAddress != null ? storeAddress : "香港九龙旺角弥敦道688号");
        res.put("store_phone", storePhone != null ? storePhone : "852-1234-5678");
        res.put("store_hours", storeHours != null ? storeHours : "07:00 - 22:00");
        return res;
    }

    /**
     * 菜单列表：按分类分组返回上架商品，填充完整图片域名
     */
    public Map<String, Object> list(String domain) {
        List<Food> foods = foodMapper.selectActiveByCategory();
        Map<Integer, Map<String, Object>> catMap = new LinkedHashMap<>();
        for (Food food : foods) {
            int cid = food.getCategoryId() != null ? food.getCategoryId() : 0;
            Map<String, Object> cat = catMap.computeIfAbsent(cid, k -> {
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("id", cid);
                m.put("name", food.getCategoryName() != null ? food.getCategoryName() : "");
                m.put("food", new ArrayList<>());
                return m;
            });
            Map<String, Object> foodMap = new LinkedHashMap<>();
            foodMap.put("id", food.getId());
            foodMap.put("category_id", food.getCategoryId());
            foodMap.put("name", food.getName());
            foodMap.put("price", food.getPrice());
            foodMap.put("image_url", domain + "/static/uploads/" + food.getImageUrl());
            ((List<Map<String, Object>>) cat.get("food")).add(foodMap);
        }
        Map<String, Object> res = new HashMap<>();
        res.put("list", new ArrayList<>(catMap.values()));
        res.put("promotion", readJsonMapList(settingService.get("promotion")));
        return res;
    }

    /**
     * 创建订单（事务）：解析购物车数据 → 查询商品价格 → 计算满减 → 插入订单 + 明细
     */
    @Transactional
    public Map<String, Object> createOrder(Integer userId, Map<String, Object> payload) {
        Object orderObj = payload.get("order");
        String comment = payload.getOrDefault("comment", "").toString().trim();
        if (!(orderObj instanceof Map<?, ?> orderMapRaw)) throw new ApiException("订单参数错误");

        Map<Integer, Integer> foodNumbers = new LinkedHashMap<>();
        for (Object value : orderMapRaw.values()) {
            if (!(value instanceof Map<?, ?> item)) continue;
            Integer foodId = CommonUtil.toInt(item.get("id"));
            Integer number = CommonUtil.toInt(item.get("number"));
            if (foodId != null && number != null && number > 0) foodNumbers.put(foodId, number);
        }
        if (foodNumbers.isEmpty()) throw new ApiException("订单为空");

        List<Food> foods = foodMapper.selectByIds(new ArrayList<>(foodNumbers.keySet()));
        if (foods.isEmpty()) throw new ApiException("商品不存在");

        BigDecimal totalPrice = BigDecimal.ZERO;
        int totalNumber = 0;
        List<Object[]> orderFoodData = new ArrayList<>();
        for (Food food : foods) {
            Integer fid = food.getId();
            Integer number = foodNumbers.get(fid);
            if (number == null || number <= 0) continue;
            BigDecimal price = food.getPrice() != null ? food.getPrice() : BigDecimal.ZERO;
            totalPrice = totalPrice.add(price.multiply(BigDecimal.valueOf(number)));
            totalNumber += number;
            orderFoodData.add(new Object[]{fid, number, price});
        }

        BigDecimal promotionPrice = calcPromotion(totalPrice);
        BigDecimal payable = totalPrice.subtract(promotionPrice).setScale(2, RoundingMode.HALF_UP);

        Order order = new Order();
        order.setUserId(userId);
        order.setPrice(payable);
        order.setPromotion(promotionPrice);
        order.setNumber(totalNumber);
        order.setComment(comment);
        order.setCreateTime(LocalDateTime.now());
        orderMapper.insert(order);
        Integer orderId = order.getId();
        if (orderId == null) throw new ApiException("订单创建失败");

        for (Object[] data : orderFoodData) {
            OrderFood of = new OrderFood();
            of.setOrderId(orderId);
            of.setFoodId((Integer) data[0]);
            of.setNumber((Integer) data[1]);
            of.setPrice((BigDecimal) data[2]);
            orderFoodMapper.insert(of);
        }
        return Map.of("order_id", orderId);
    }

    /**
     * 更新订单备注（事务）
     */
    @Transactional
    public Map<String, Object> commentOrder(Integer userId, Integer id, String comment) {
        int n = orderMapper.updateComment(id, userId, comment == null ? "" : comment.trim());
        if (n <= 0) throw new ApiException("订单备注添加失败");
        return Map.of("msg", "订单备注添加成功");
    }

    /**
     * 查询订单详情（含商品明细和图片）
     */
    public Map<String, Object> getOrder(Integer userId, Integer id, String domain) {
        Order order = orderMapper.selectById(id);
        if (order == null || !Objects.equals(order.getUserId(), userId)) throw new ApiException("订单不存在");
        List<Map<String, Object>> items = orderFoodMapper.selectByOrderIdWithFood(id);
        for (Map<String, Object> item : items) {
            if (item.get("image_url") != null)
                item.put("image_url", domain + "/static/uploads/" + item.get("image_url"));
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("id", order.getId());
        result.put("price", order.getPrice());
        result.put("promotion", order.getPromotion());
        result.put("number", order.getNumber());
        result.put("is_pay", order.getIsPay() == 1);
        result.put("is_taken", order.getIsTaken() == 1);
        result.put("comment", order.getComment());
        result.put("create_time", order.getCreateTime());
        result.put("pay_time", order.getPayTime());
        result.put("taken_time", order.getTakenTime());
        result.put("sn", "WX" + String.format("%014d", id));
        result.put("code", "A" + String.format("%02d", id));
        result.put("order_food", items);
        return result;
    }

    /**
     * 支付（事务）：标记支付 → 累加用户消费金额
     */
    @Transactional
    public Map<String, Object> pay(Integer userId, Integer id) {
        Order order = orderMapper.selectByUserAndId(id, userId);
        if (order == null || order.getIsPay() == 1) throw new ApiException("支付失败");
        orderMapper.updatePay(id, LocalDateTime.now());
        userMapper.addPrice(userId, order.getPrice());
        return Map.of("msg", "支付成功");
    }

    /**
     * 用户订单列表（游标分页，消除 N+1 查询）
     */
    public Map<String, Object> getOrderList(Integer userId, Integer lastId, Integer row) {
        int pageSize = Math.max(1, Math.min(row == null ? 10 : row, 99));
        Integer lid = lastId == null ? 0 : lastId;
        List<Order> orders = orderMapper.selectUserOrdersWithFoods(userId, lid > 0 ? lid : null, pageSize);
        List<Map<String, Object>> list = new ArrayList<>();
        for (Order order : orders) {
            Map<String, Object> item = new LinkedHashMap<>();
            Integer oid = order.getId();
            item.put("id", oid);
            item.put("price", order.getPrice());
            item.put("promotion", order.getPromotion());
            item.put("number", order.getNumber());
            item.put("is_pay", order.getIsPay() == 1);
            item.put("is_taken", order.getIsTaken() == 1);
            item.put("comment", order.getComment());
            item.put("create_time", order.getCreateTime());
            item.put("pay_time", order.getPayTime());
            List<Map<String, Object>> items = new ArrayList<>();
            if (order.getOrderFoods() != null) {
                for (OrderFood of : order.getOrderFoods()) {
                    Map<String, Object> foodRow = new LinkedHashMap<>();
                    foodRow.put("food_id", of.getFoodId());
                    foodRow.put("number", of.getNumber());
                    foodRow.put("price", of.getPrice());
                    foodRow.put("name", of.getFoodName());
                    items.add(foodRow);
                }
            }
            item.put("first_food_name", items.isEmpty() ? "" : items.get(0).get("name"));
            item.put("order_food", items);
            list.add(item);
        }
        Integer nextLastId = list.isEmpty() ? 0 : ((Number) list.get(list.size() - 1).get("id")).intValue();
        return Map.of("list", list, "last_id", nextLastId);
    }

    /**
     * 用户消费记录
     */
    public Map<String, Object> record(Integer userId) {
        List<Map<String, Object>> records = orderMapper.selectRecordsByUserId(userId);
        return Map.of("list", records);
    }

    /**
     * 计算满减优惠：选择最优方案（满额减最多）
     */
    private BigDecimal calcPromotion(BigDecimal totalPrice) {
        List<Map<String, Object>> promotionList = readJsonMapList(settingService.get("promotion"));
        BigDecimal promotionPrice = BigDecimal.ZERO;
        BigDecimal maxDiff = BigDecimal.valueOf(-1);
        for (Map<String, Object> p : promotionList) {
            BigDecimal k = CommonUtil.toDecimal(p.get("k"));
            BigDecimal v = CommonUtil.toDecimal(p.get("v"));
            BigDecimal diff = totalPrice.subtract(k);
            if (diff.compareTo(BigDecimal.ZERO) > 0 && diff.compareTo(maxDiff) > 0) {
                maxDiff = diff;
                promotionPrice = v;
            }
        }
        return promotionPrice;
    }

    /**
     * 从 JSON 字符串解析 List<String>
     */
    private List<String> readJsonList(String json) {
        if (json == null || json.isBlank()) return List.of();
        try {
            return objectMapper.readValue(json, new TypeReference<>() {
            });
        } catch (Exception e) {
            return List.of();
        }
    }

    /**
     * 从 JSON 字符串解析 List<Map>
     */
    private List<Map<String, Object>> readJsonMapList(String json) {
        if (json == null || json.isBlank()) return List.of();
        try {
            return objectMapper.readValue(json, new TypeReference<>() {
            });
        } catch (Exception e) {
            return List.of();
        }
    }
}
