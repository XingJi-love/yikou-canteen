package fun.xingji.wxshop.controller.admin;

import fun.xingji.wxshop.service.AdminSettingService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 管理端系统配置 API
 * 读写小程序设置（AppID、轮播图、满减优惠等）
 * 路径前缀: /admin/api/settings
 */
@RestController
@RequestMapping("/admin/api/settings")
public class AdminSettingController {

    private final AdminSettingService adminSettingService;

    public AdminSettingController(AdminSettingService adminSettingService) {
        this.adminSettingService = adminSettingService;
    }

    /**
     * GET - 读取所有配置
     */
    @GetMapping
    public Map<String, Object> get() {
        return adminSettingService.getSettings();
    }

    /**
     * POST - 保存所有配置
     */
    @PostMapping
    public Map<String, Object> save(@RequestBody Map<String, Object> body) {
        return adminSettingService.save(body);
    }
}
