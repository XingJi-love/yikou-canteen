package fun.xingji.wxshop.controller;

import fun.xingji.wxshop.entity.User;
import fun.xingji.wxshop.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;

/**
 * 小程序用户登录 API 控制器
 * 处理微信静默登录（code2Session）和登录状态检查
 * 路径前缀: /api/user
 */
@RestController
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /** GET /api/user/checkLogin - 检查用户登录状态 */
    @GetMapping("/checkLogin")
    public Map<String, Object> checkLogin(HttpSession session) {
        Map<String, Object> result = new HashMap<>();
        result.put("isLogin", userService.checkLogin(session));
        if (userService.checkLogin(session)) {
            result.put("token", session.getId());
        }
        return result;
    }

    /** GET /api/user/login - 微信小程序静默登录（使用 js_code） */
    @GetMapping("/login")
    public Map<String, Object> login(@RequestParam("js_code") String jsCode, HttpSession session) {
        boolean success = userService.login(jsCode, session);
        Map<String, Object> result = new HashMap<>();
        result.put("isLogin", success);
        if (success) {
            result.put("token", session.getId());
        }
        return result;
    }

    /** GET /api/user/profile - 获取当前用户个人信息 */
    @GetMapping("/profile")
    public Map<String, Object> getProfile(HttpServletRequest request, HttpSession session) {
        String scheme = request.getScheme();
        String serverName = request.getServerName();
        int port = request.getServerPort();
        String domain = scheme + "://" + serverName + (port == 80 || port == 443 ? "" : ":" + port);
        return userService.getProfile(session, domain);
    }

    /** PUT /api/user/profile - 更新当前用户个人信息 */
    @PutMapping("/profile")
    public Map<String, Object> updateProfile(@RequestBody User profile, HttpSession session) {
        boolean success = userService.updateProfile(profile, session);
        Map<String, Object> result = new HashMap<>();
        result.put("success", success);
        return result;
    }
}
