package fun.xingji.wxshop.controller;

import fun.xingji.wxshop.exception.ApiException;
import fun.xingji.wxshop.service.FoodService;
import fun.xingji.wxshop.util.CommonUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 小程序端商品与订单 API 控制器
 * 处理首页、菜单、下单、支付、订单列表、消费记录等请求
 * 路径前缀: /api/food
 */
@RestController
@RequestMapping("/api/food")
public class FoodController {

    private final FoodService foodService;

    public FoodController(FoodService foodService) {
        this.foodService = foodService;
    }

    /**
     * GET /api/food/index - 首页数据
     */
    @GetMapping("/index")
    public Map<String, Object> index(HttpServletRequest request) {
        return foodService.index(domain(request));
    }

    /**
     * GET /api/food/list - 菜单列表
     */
    @GetMapping("/list")
    public Map<String, Object> list(HttpServletRequest request) {
        return foodService.list(domain(request));
    }

    /**
     * GET /api/food/detail - 菜品详情
     */
    @GetMapping("/detail")
    public Map<String, Object> detail(@RequestParam("id") Integer id, HttpServletRequest request) {
        return foodService.detail(id, domain(request));
    }

    @GetMapping("/order")
    public Map<String, Object> order(@RequestParam("id") Integer id, HttpSession session, HttpServletRequest request) {
        return foodService.getOrder(userId(session), id, domain(request));
    }

    /**
     * POST /api/food/order - 创建订单或添加备注
     */
    @PostMapping("/order")
    public Map<String, Object> orderPost(@RequestBody(required = false) Map<String, Object> body, HttpSession session) {
        Map<String, Object> payload = body == null ? new HashMap<>() : body;
        Integer id = CommonUtil.toInt(payload.get("id"));
        if (id != null && id > 0) {
            String comment = payload.get("comment") == null ? "" : String.valueOf(payload.get("comment"));
            return foodService.commentOrder(userId(session), id, comment);
        }
        return foodService.createOrder(userId(session), payload);
    }

    /**
     * POST /api/food/pay - 订单支付
     */
    @PostMapping("/pay")
    public Map<String, Object> pay(@RequestBody Map<String, Object> body, HttpSession session) {
        Integer id = CommonUtil.toInt(body.get("id"));
        if (id == null || id <= 0) {
            throw new ApiException("支付失败");
        }
        return foodService.pay(userId(session), id);
    }

    /**
     * GET /api/food/orderlist - 用户订单列表（游标分页）
     */
    @GetMapping("/orderlist")
    public Map<String, Object> orderList(@RequestParam(name = "last_id", required = false) Integer lastId,
                                         @RequestParam(name = "row", required = false) Integer row,
                                         HttpSession session) {
        return foodService.getOrderList(userId(session), lastId, row);
    }

    /**
     * GET /api/food/record - 用户消费记录
     */
    @GetMapping("/record")
    public Map<String, Object> record(HttpSession session) {
        return foodService.record(userId(session));
    }

    /**
     * 从 Session 获取当前登录用户ID
     */
    private Integer userId(HttpSession session) {
        Object id = session.getAttribute("userId");
        return CommonUtil.toInt(id);
    }

    /**
     * 构建当前请求的完整域名（scheme://host:port）
     */
    private String domain(HttpServletRequest request) {
        return request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort();
    }
}
