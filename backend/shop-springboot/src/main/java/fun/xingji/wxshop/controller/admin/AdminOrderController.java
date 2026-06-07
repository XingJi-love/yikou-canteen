package fun.xingji.wxshop.controller.admin;

import fun.xingji.wxshop.service.AdminOrderService;
import fun.xingji.wxshop.util.CommonUtil;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 管理端订单管理 API
 * 订单列表查询（支持多条件筛选）、标记取餐
 * 路径前缀: /admin/api/orders
 */
@RestController
@RequestMapping("/admin/api/orders")
public class AdminOrderController {

    private final AdminOrderService adminOrderService;

    public AdminOrderController(AdminOrderService adminOrderService) {
        this.adminOrderService = adminOrderService;
    }

    /**
     * GET - 分页查询订单列表（支持按支付状态、取餐状态、用户、订单号筛选）
     */
    @GetMapping
    public Map<String, Object> list(@RequestParam(name = "is_pay", required = false) Integer isPay,
                                    @RequestParam(name = "is_taken", required = false) Integer isTaken,
                                    @RequestParam(required = false) String search,
                                    @RequestParam(name = "user_id", required = false) Integer userId,
                                    @RequestParam(defaultValue = "1") int page,
                                    @RequestParam(name = "page_size", defaultValue = "10") int pageSize) {
        return adminOrderService.list(userId, isPay, isTaken, search, page, pageSize);
    }

    /**
     * POST /{id}/taken - 设置订单取餐状态
     */
    @PostMapping("/{id}/taken")
    public Map<String, Object> taken(@PathVariable Integer id, @RequestBody Map<String, Object> body) {
        boolean val = Boolean.TRUE.equals(body.get("taken")) || CommonUtil.toInt(body.get("taken")) == 1;
        return adminOrderService.setTaken(id, val);
    }
}
