package fun.xingji.wxshop.controller.admin;

import fun.xingji.wxshop.service.AdminCategoryService;
import fun.xingji.wxshop.util.CommonUtil;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 管理端分类管理 API
 * CRUD 商品分类，支持排序
 * 路径前缀: /admin/api/categories
 */
@RestController
@RequestMapping("/admin/api/categories")
public class AdminCategoryController {

    private final AdminCategoryService adminCategoryService;

    public AdminCategoryController(AdminCategoryService adminCategoryService) {
        this.adminCategoryService = adminCategoryService;
    }

    /**
     * GET - 查询所有分类
     */
    @GetMapping
    public List<Map<String, Object>> list() {
        return adminCategoryService.list();
    }

    /**
     * POST - 添加分类
     */
    @PostMapping
    public Map<String, Object> add(@RequestBody Map<String, Object> body) {
        return adminCategoryService.add(String.valueOf(body.getOrDefault("name", "")),
                CommonUtil.toInt(body.get("sort")));
    }

    /**
     * PUT /{id} - 更新分类信息
     */
    @PutMapping("/{id}")
    public Map<String, Object> update(@PathVariable Integer id, @RequestBody Map<String, Object> body) {
        return adminCategoryService.update(id, String.valueOf(body.getOrDefault("name", "")),
                CommonUtil.toInt(body.get("sort")));
    }

    /**
     * POST /sort - 批量保存排序
     */
    @PostMapping("/sort")
    public Map<String, Object> sort(@RequestBody Map<String, Object> body) {
        return adminCategoryService.sort(body);
    }

    /**
     * DELETE /{id} - 删除分类
     */
    @DeleteMapping("/{id}")
    public Map<String, Object> delete(@PathVariable Integer id) {
        return adminCategoryService.delete(id);
    }
}
