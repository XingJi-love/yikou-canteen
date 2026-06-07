package fun.xingji.wxshop.controller.admin;

import fun.xingji.wxshop.entity.User;
import fun.xingji.wxshop.service.AdminUserService;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 管理端用户管理 API
 * 分页查询小程序注册用户列表
 * 路径前缀: /admin/api/users
 */
@RestController
@RequestMapping("/admin/api/users")
public class AdminUserController {

    private final AdminUserService adminUserService;

    public AdminUserController(AdminUserService adminUserService) {
        this.adminUserService = adminUserService;
    }

    /**
     * GET - 分页查询用户列表
     */
    @GetMapping
    public Map<String, Object> list(@RequestParam(defaultValue = "1") int page,
                                    @RequestParam(name = "page_size", defaultValue = "10") int pageSize) {
        return adminUserService.list(page, pageSize);
    }

    /**
     * GET /admin/api/users/{id} - 获取单个用户详情
     */
    @GetMapping("/{id}")
    public Map<String, Object> getUserById(@PathVariable Integer id) {
        return adminUserService.getUserById(id);
    }

    /**
     * PUT /admin/api/users/{id} - 更新用户信息
     */
    @PutMapping("/{id}")
    public Map<String, Object> updateUser(@PathVariable Integer id, @RequestBody User user) {
        user.setId(id);
        boolean success = adminUserService.updateUser(user);
        Map<String, Object> result = new HashMap<>();
        result.put("success", success);
        return result;
    }
}
