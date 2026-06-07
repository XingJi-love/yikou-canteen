package fun.xingji.wxshop.service;

import fun.xingji.wxshop.entity.Category;
import fun.xingji.wxshop.exception.ApiException;
import fun.xingji.wxshop.mapper.CategoryMapper;
import fun.xingji.wxshop.util.CommonUtil;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.*;

/**
 * 管理端分类服务
 * 提供分类的列表查询、增删改、批量排序
 */
@Service
public class AdminCategoryService extends BaseService {

    private final CategoryMapper categoryMapper;

    public AdminCategoryService(CategoryMapper categoryMapper) {
        this.categoryMapper = categoryMapper;
    }

    /** 查询全部分类列表（按 sort 升序） */
    public List<Map<String, Object>> list() {
        List<Category> categories = categoryMapper.selectAll();
        List<Map<String, Object>> result = new ArrayList<>();
        for (Category cat : categories) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id", cat.getId()); map.put("name", cat.getName()); map.put("sort", cat.getSort());
            result.add(map);
        }
        return result;
    }

    /** 添加分类 */
    public Map<String, Object> add(String name, Integer sort) {
        validateName(name);
        Category category = new Category();
        category.setName(name.trim());
        category.setSort(sort == null ? 0 : sort);
        categoryMapper.insert(category);
        return Map.of("msg", "添加分类成功");
    }

    /** 更新分类 */
    public Map<String, Object> update(Integer id, String name, Integer sort) {
        if (id == null || id <= 0) throw new ApiException("指定分类不存在");
        validateName(name);
        Category category = new Category();
        category.setId(id); category.setName(name.trim());
        category.setSort(sort == null ? 0 : sort);
        if (categoryMapper.update(category) <= 0) throw new ApiException("指定分类不存在");
        return Map.of("msg", "修改分类成功");
    }

    /** 批量保存排序 */
    public Map<String, Object> sort(Map<String, Object> sortMap) {
        if (sortMap == null || sortMap.isEmpty()) return Map.of("msg", "保存排序成功");
        for (Map.Entry<String, Object> entry : sortMap.entrySet()) {
            Integer id = CommonUtil.toInt(entry.getKey());
            Integer sort = CommonUtil.toInt(entry.getValue());
            if (id != null && sort != null) categoryMapper.updateSort(id, sort);
        }
        return Map.of("msg", "保存排序成功");
    }

    /** 删除分类 */
    public Map<String, Object> delete(Integer id) {
        if (id == null || id <= 0) throw new ApiException("指定分类不存在");
        if (categoryMapper.deleteById(id) <= 0) throw new ApiException("指定分类不存在");
        return Map.of("msg", "删除分类成功");
    }

    private void validateName(String name) {
        if (!StringUtils.hasText(name)) throw new ApiException("分类名称不能为空");
    }
}
