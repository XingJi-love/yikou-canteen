package fun.xingji.wxshop.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * 管理端页面路由控制器
 * 将 /admin 和 /admin/ 重定向到静态管理后台首页
 */
@Controller
public class AdminPageController {

    /**
     * 管理后台首页 → 重定向到 Vue Admin SPA 入口 index.html
     */
    @GetMapping({"/admin", "/admin/"})
    public String adminIndex() {
        return "redirect:/admin/index.html";
    }
}
