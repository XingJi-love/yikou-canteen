package fun.xingji.wxshop.service;

import fun.xingji.wxshop.entity.User;
import fun.xingji.wxshop.exception.ApiException;
import fun.xingji.wxshop.mapper.UserMapper;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 管理端用户服务
 * 提供用户分页列表、查询详情、编辑用户信息
 */
@Service
public class AdminUserService extends BaseService {

    private final UserMapper userMapper;

    public AdminUserService(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    /** 用户分页查询 */
    public Map<String, Object> list(int page, int pageSize) {
        int currentPage = Math.max(page, 1);
        int size = Math.min(Math.max(pageSize, 1), 100);
        int offset = (currentPage - 1) * size;
        int total = userMapper.count();
        List<User> users = userMapper.selectPage(offset, size);
        List<Map<String, Object>> list = new ArrayList<>();
        for (User user : users) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id", user.getId());
            map.put("openid", user.getOpenid());
            map.put("nickname", user.getNickname());
            map.put("avatarUrl", user.getAvatarUrl());
            map.put("phone", user.getPhone());
            map.put("gender", user.getGender());
            map.put("price", user.getPrice());
            map.put("create_time", user.getCreateTime());
            list.add(map);
        }
        Map<String, Object> res = new HashMap<>();
        res.put("list", list);
        res.put("total", total);
        res.put("page", currentPage);
        res.put("pageSize", size);
        return res;
    }

    /** 根据 ID 查询用户详情 */
    public Map<String, Object> getUserById(Integer id) {
        User user = userMapper.selectById(id);
        if (user == null) throw new ApiException("用户不存在");
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", user.getId());
        map.put("openid", user.getOpenid());
        map.put("nickname", user.getNickname());
        map.put("avatarUrl", user.getAvatarUrl());
        map.put("phone", user.getPhone());
        map.put("gender", user.getGender());
        map.put("price", user.getPrice());
        map.put("create_time", user.getCreateTime());
        return map;
    }

    /** 更新用户信息 */
    public boolean updateUser(User user) {
        return userMapper.updateById(user) > 0;
    }
}
