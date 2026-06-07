package fun.xingji.wxshop.controller.admin;

import fun.xingji.wxshop.service.AdminFoodService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 管理端商品管理 API
 * CRUD 商品，支持分页、搜索、回收站
 * 路径前缀: /admin/api/foods
 */
@RestController
@RequestMapping("/admin/api/foods")
public class AdminFoodController {

    private final AdminFoodService adminFoodService;

    public AdminFoodController(AdminFoodService adminFoodService) {
        this.adminFoodService = adminFoodService;
    }

    /**
     * GET - 分页查询商品列表
     */
    @GetMapping
    public Map<String, Object> list(@RequestParam(name = "category_id", required = false) Integer categoryId,
                                    @RequestParam(required = false) String search,
                                    @RequestParam(defaultValue = "0") int recycle,
                                    @RequestParam(defaultValue = "1") int page,
                                    @RequestParam(name = "page_size", defaultValue = "10") int pageSize) {
        return adminFoodService.list(categoryId, search, recycle == 1, page, pageSize);
    }

    /**
     * GET /{id} - 查询商品详情
     */
    @GetMapping("/{id}")
    public Map<String, Object> get(@PathVariable Integer id) {
        return adminFoodService.get(id);
    }

    /**
     * POST - 新增商品
     */
    @PostMapping
    public Map<String, Object> create(@RequestBody Map<String, Object> body) {
        return adminFoodService.save(null, body);
    }

    /**
     * PUT /{id} - 更新商品
     */
    @PutMapping("/{id}")
    public Map<String, Object> update(@PathVariable Integer id, @RequestBody Map<String, Object> body) {
        return adminFoodService.save(id, body);
    }

    /**
     * DELETE /{id} - 删除商品（recycle=1 彻底删除，否则软删除）
     */
    @DeleteMapping("/{id}")
    public Map<String, Object> delete(@PathVariable Integer id,
                                      @RequestParam(defaultValue = "0") int recycle) {
        return adminFoodService.delete(id, recycle == 1);
    }
}
