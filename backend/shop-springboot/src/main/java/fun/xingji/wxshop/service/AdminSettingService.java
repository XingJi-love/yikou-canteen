package fun.xingji.wxshop.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import fun.xingji.wxshop.exception.ApiException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 管理端系统配置服务
 * 读取/保存小程序设置（AppID/Secret、满减规则、轮播图、今日推荐、店铺信息等）
 * JSON 类型的配置项自动序列化/反序列化
 */
@Service
public class AdminSettingService extends BaseService {

    private final SettingService settingService;
    private final ObjectMapper objectMapper;

    public AdminSettingService(SettingService settingService, ObjectMapper objectMapper) {
        this.settingService = settingService;
        this.objectMapper = objectMapper;
    }

    /** 读取全部系统配置 */
    public Map<String, Object> getSettings() {
        Map<String, Object> res = new HashMap<>();
        res.put("appid", settingService.get("appid"));
        res.put("appsecret", settingService.get("appsecret"));
        res.put("promotion", readJsonMapList(settingService.get("promotion")));
        res.put("img_swiper", readJsonList(settingService.get("img_swiper")));
        res.put("img_ad", settingService.get("img_ad"));
        res.put("img_category", readJsonList(settingService.get("img_category")));
        res.put("recommend_img", settingService.get("recommend_img"));
        res.put("recommend_name", settingService.get("recommend_name"));
        res.put("recommend_price", settingService.get("recommend_price"));
        res.put("store_name", settingService.get("store_name"));
        res.put("store_subtitle", settingService.get("store_subtitle"));
        res.put("store_description", settingService.get("store_description"));
        res.put("store_address", settingService.get("store_address"));
        res.put("store_phone", settingService.get("store_phone"));
        res.put("store_hours", settingService.get("store_hours"));
        return res;
    }

    /** 保存系统配置（JSON 数组自动序列化） */
    public Map<String, Object> save(Map<String, Object> payload) {
        Map<String, String> settings = new HashMap<>();
        settings.put("appid", stringValue(payload.get("appid")));
        settings.put("appsecret", stringValue(payload.get("appsecret")));
        settings.put("promotion", writeJson(payload.get("promotion")));
        settings.put("img_swiper", writeJson(payload.get("img_swiper")));
        settings.put("img_ad", stringValue(payload.get("img_ad")));
        settings.put("img_category", writeJson(payload.get("img_category")));
        settings.put("recommend_img", stringValue(payload.get("recommend_img")));
        settings.put("recommend_name", stringValue(payload.get("recommend_name")));
        settings.put("recommend_price", stringValue(payload.get("recommend_price")));
        settings.put("store_name", stringValue(payload.get("store_name")));
        settings.put("store_subtitle", stringValue(payload.get("store_subtitle")));
        settings.put("store_description", stringValue(payload.get("store_description")));
        settings.put("store_address", stringValue(payload.get("store_address")));
        settings.put("store_phone", stringValue(payload.get("store_phone")));
        settings.put("store_hours", stringValue(payload.get("store_hours")));
        settingService.setAll(settings);
        return Map.of("msg", "保存成功");
    }

    private String stringValue(Object value) { return value == null ? "" : String.valueOf(value).trim(); }
    private String writeJson(Object value) {
        if (value == null) return "[]";
        try { return objectMapper.writeValueAsString(value); }
        catch (Exception e) { throw new ApiException("配置格式错误"); }
    }
    private List<String> readJsonList(String json) {
        if (!StringUtils.hasText(json)) return List.of();
        try { return objectMapper.readValue(json, new TypeReference<>() {}); }
        catch (Exception e) { return List.of(); }
    }
    private List<Map<String, Object>> readJsonMapList(String json) {
        if (!StringUtils.hasText(json)) return List.of();
        try { return objectMapper.readValue(json, new TypeReference<>() {}); }
        catch (Exception e) { return List.of(); }
    }
}
