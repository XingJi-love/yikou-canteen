package fun.xingji.wxshop.service;

import fun.xingji.wxshop.entity.Admin;
import fun.xingji.wxshop.exception.ApiException;
import fun.xingji.wxshop.mapper.AdminMapper;
import fun.xingji.wxshop.util.AdminPassword;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Map;

/**
 * 管理员认证服务
 * 提供后台登录/登出/改密/状态检查，以及默认管理员初始化
 */
@Service
public class AdminAuthService extends BaseService {

    private final AdminMapper adminMapper;

    public AdminAuthService(AdminMapper adminMapper) {
        this.adminMapper = adminMapper;
    }

    /** 检查 Session 中是否存在 adminId */
    public boolean checkLogin(HttpSession session) {
        return session.getAttribute("adminId") != null;
    }

    /** 管理员登录（用户名+密码，MD5 校验） */
    public Map<String, Object> login(String username, String password, HttpSession session) {
        if (!StringUtils.hasText(username) || !StringUtils.hasText(password))
            throw new ApiException("用户名或密码不正确");
        Admin admin = adminMapper.selectByUsername(username.trim());
        if (admin == null) throw new ApiException("用户名或密码不正确");
        if (!AdminPassword.matches(password, admin.getPassword()))
            throw new ApiException("用户名或密码不正确");
        session.setAttribute("adminId", admin.getId());
        session.setAttribute("adminUsername", admin.getUsername());
        return Map.of("isLogin", true, "username", admin.getUsername());
    }

    /** 管理员登出（清除 Session） */
    public Map<String, Object> logout(HttpSession session) {
        session.removeAttribute("adminId");
        session.removeAttribute("adminUsername");
        return Map.of("msg", "退出成功");
    }

    /** 修改当前管理员密码 */
    public Map<String, Object> changePassword(HttpSession session, String password) {
        Object adminIdObj = session.getAttribute("adminId");
        if (adminIdObj == null) throw new ApiException("管理员未登录");
        if (!StringUtils.hasText(password)) throw new ApiException("密码不能为空");
        int adminId = ((Number) adminIdObj).intValue();
        String hashed = AdminPassword.hash(password.trim());
        Admin admin = new Admin();
        admin.setId(adminId);
        admin.setPassword(hashed);
        adminMapper.updatePassword(admin);
        return Map.of("msg", "密码修改成功");
    }

    /** 确保默认管理员存在（无管理员时创建 admin/123456） */
    public void ensureDefaultAdmin() {
        if (adminMapper.count() > 0) return;
        Admin admin = new Admin();
        admin.setUsername("admin");
        admin.setPassword(AdminPassword.hash("123456"));
        adminMapper.insert(admin);
    }
}
