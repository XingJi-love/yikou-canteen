package fun.xingji.wxshop.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import fun.xingji.wxshop.entity.User;
import fun.xingji.wxshop.exception.ApiException;
import fun.xingji.wxshop.mapper.UserMapper;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.HashMap;
import java.util.Map;

/**
 * 小程序用户服务
 * 处理微信静默登录（code2Session）、登录状态检查、用户信息查询/更新
 */
@Service
public class UserService extends BaseService {

    private final UserMapper userMapper;
    private final SettingService settingService;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public UserService(UserMapper userMapper, SettingService settingService,
                       RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.userMapper = userMapper;
        this.settingService = settingService;
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    /** 检查 Session 中是否存在 userId（即是否已登录） */
    public boolean checkLogin(HttpSession session) {
        return session.getAttribute("userId") != null;
    }

    /**
     * 微信静默登录流程
     * 1. 用 js_code 调微信接口获取 openid
     * 2. 根据 openid 查找或创建用户
     * 3. 将 userId 存入 Session
     */
    public boolean login(String jsCode, HttpSession session) {
        if (!StringUtils.hasText(jsCode)) return false;
        String appid = settingService.get("appid");
        String secret = settingService.get("appsecret");
        if (!StringUtils.hasText(appid) || !StringUtils.hasText(secret)) return false;

        String url = UriComponentsBuilder.fromHttpUrl("https://api.weixin.qq.com/sns/jscode2session")
                .queryParam("appid", appid).queryParam("secret", secret)
                .queryParam("js_code", jsCode).queryParam("grant_type", "authorization_code")
                .build(true).toUriString();

        String result = restTemplate.getForObject(url, String.class);
        try {
            Map<String, Object> data = objectMapper.readValue(result, new TypeReference<>() {});
            Object openidObj = data.get("openid");
            if (openidObj == null) return false;
            String openid = String.valueOf(openidObj);

            User user = userMapper.selectByOpenid(openid);
            if (user == null) {
                User newUser = new User();
                newUser.setOpenid(openid);
                userMapper.insert(newUser);
                user = newUser;
            }
            if (user.getId() == null) throw new ApiException("登录失败");
            session.setAttribute("userId", user.getId());
            session.setAttribute("openid", openid);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /** 获取当前用户个人信息（头像 URL 自动补全域名） */
    public Map<String, Object> getProfile(HttpSession session, String domain) {
        Integer userId = (Integer) session.getAttribute("userId");
        if (userId == null) throw new ApiException("请先登录");
        User user = userMapper.selectById(userId);
        if (user == null) throw new ApiException("用户不存在");
        Map<String, Object> profile = new HashMap<>();
        profile.put("id", user.getId());
        profile.put("nickname", user.getNickname());
        String avatarUrl = user.getAvatarUrl();
        if (avatarUrl != null && !avatarUrl.isEmpty() && !avatarUrl.startsWith("http")) {
            avatarUrl = domain + avatarUrl;
        }
        profile.put("avatarUrl", avatarUrl);
        profile.put("phone", user.getPhone());
        profile.put("gender", user.getGender());
        profile.put("price", user.getPrice());
        return profile;
    }

    /** 更新当前用户个人资料 */
    public boolean updateProfile(User profile, HttpSession session) {
        Integer userId = (Integer) session.getAttribute("userId");
        if (userId == null) throw new ApiException("请先登录");
        profile.setId(userId);
        return userMapper.updateProfile(profile) > 0;
    }
}
