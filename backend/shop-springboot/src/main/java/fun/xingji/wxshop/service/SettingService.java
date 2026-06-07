package fun.xingji.wxshop.service;

import fun.xingji.wxshop.entity.Setting;
import fun.xingji.wxshop.mapper.SettingMapper;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * 系统配置服务
 * 提供字符串键值对的读写，支持批量保存
 * 底层使用 INSERT ... ON DUPLICATE KEY UPDATE
 */
@Service
public class SettingService extends BaseService {

    private final SettingMapper settingMapper;

    public SettingService(SettingMapper settingMapper) {
        this.settingMapper = settingMapper;
    }

    /** 读取配置值，不存在则返回空字符串 */
    public String get(String name) {
        Setting setting = settingMapper.selectByName(name);
        return setting != null ? setting.getValue() : "";
    }

    /** 写入单条配置 */
    public void set(String name, String value) {
        Setting setting = new Setting();
        setting.setName(name);
        setting.setValue(value == null ? "" : value);
        settingMapper.insertOrUpdate(setting);
    }

    /** 批量写入配置 */
    public void setAll(Map<String, String> settings) {
        settings.forEach(this::set);
    }
}
