package fun.xingji.wxshop.controller.admin;

import fun.xingji.wxshop.service.AdminAuthService;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 管理端认证 API 控制器
 * 处理管理员登录、登出、修改密码和登录状态检查
 * 路径前缀: /admin/api
 */
@RestController
@RequestMapping("/admin/api")
public class AdminAuthController {

    private final AdminAuthService adminAuthService;

    public AdminAuthController(AdminAuthService adminAuthService) {
        this.adminAuthService = adminAuthService;
    }

    /**
     * GET /admin/api/checkLogin - 检查管理员登录状态
     */
    @GetMapping("/checkLogin")
    public Map<String, Object> checkLogin(HttpSession session) {
        boolean isLogin = adminAuthService.checkLogin(session);
        Map<String, Object> res = new HashMap<>();
        res.put("isLogin", isLogin);
        if (isLogin) {
            res.put("username", session.getAttribute("adminUsername"));
        }
        return res;
    }

    /**
     * POST /admin/api/login - 管理员登录
     */
    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody Map<String, String> body, HttpSession session) {
        return adminAuthService.login(body.get("username"), body.get("password"), session);
    }

    /**
     * POST /admin/api/logout - 管理员登出
     */
    @PostMapping("/logout")
    public Map<String, Object> logout(HttpSession session) {
        return adminAuthService.logout(session);
    }

    /**
     * POST /admin/api/password - 修改管理员密码
     */
    @PostMapping("/password")
    public Map<String, Object> changePassword(@RequestBody Map<String, String> body, HttpSession session) {
        return adminAuthService.changePassword(session, body.get("password"));
    }
}
