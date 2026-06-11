package fun.xingji.wxshop.service;

import fun.xingji.wxshop.entity.Category;
import fun.xingji.wxshop.entity.Food;
import fun.xingji.wxshop.exception.ApiException;
import fun.xingji.wxshop.mapper.CategoryMapper;
import fun.xingji.wxshop.mapper.FoodMapper;
import fun.xingji.wxshop.util.CommonUtil;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

/**
 * 管理端商品服务
 * 提供商品列表（分类/搜索/回收站筛选）、单商品查询、新增/编辑、删除（软/硬）、图片管理
 */
@Service
public class AdminFoodService extends BaseService {

    private final FoodMapper foodMapper;
    private final CategoryMapper categoryMapper;

    public AdminFoodService(FoodMapper foodMapper, CategoryMapper categoryMapper) {
        this.foodMapper = foodMapper;
        this.categoryMapper = categoryMapper;
    }

    /** 分页搜索商品列表（支持分类筛选、关键词搜索、回收站模式） */
    public Map<String, Object> list(Integer categoryId, String search, boolean recycle, int page, int pageSize) {
        int currentPage = Math.max(page, 1);
        int size = Math.min(Math.max(pageSize, 1), 100);
        int offset = (currentPage - 1) * size;
        String rawSearch = StringUtils.hasText(search) ? search.trim() : null;
        int total = foodMapper.countBySearch(categoryId, rawSearch, recycle);
        List<Food> foods = foodMapper.selectPageBySearch(categoryId, rawSearch, recycle, offset, size);

        Map<Integer, String> catNameMap = new HashMap<>();
        for (Category c : categoryMapper.selectAll()) catNameMap.put(c.getId(), c.getName());

        List<Map<String, Object>> list = new ArrayList<>();
        for (Food food : foods) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id", food.getId());
            map.put("category_id", food.getCategoryId());
            map.put("category_name", catNameMap.getOrDefault(food.getCategoryId(), ""));
            map.put("name", food.getName());
            map.put("price", food.getPrice());
            map.put("image_url", food.getImageUrl());
            map.put("status", food.getStatus());
            map.put("create_time", food.getCreateTime());
            map.put("update_time", food.getUpdateTime());
            map.put("delete_time", food.getDeleteTime());
            map.put("description", food.getDescription());
            list.add(map);
        }
        Map<String, Object> res = new HashMap<>();
        res.put("list", list); res.put("total", total);
        res.put("page", currentPage); res.put("pageSize", size);
        return res;
    }

    /** 查询单个商品 */
    public Map<String, Object> get(Integer id) {
        Food food = foodMapper.selectById(id);
        if (food == null) throw new ApiException("商品记录不存在");
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("id", food.getId());
        row.put("category_id", food.getCategoryId());
        row.put("name", food.getName());
        row.put("price", food.getPrice());
        row.put("image_url", food.getImageUrl());
        row.put("status", food.getStatus());
        row.put("description", food.getDescription());
        return row;
    }

    /** 新增或保存商品 */
    public Map<String, Object> save(Integer id, Map<String, Object> payload) {
        Integer categoryId = CommonUtil.toInt(payload.get("category_id"));
        String name = payload.get("name") == null ? "" : String.valueOf(payload.get("name")).trim();
        BigDecimal price = CommonUtil.toDecimal(payload.get("price"));
        int status = CommonUtil.toInt(payload.get("status")) == null ? 0 : CommonUtil.toInt(payload.get("status"));
        String imageUrl = payload.get("image_url") == null ? "" : String.valueOf(payload.get("image_url")).trim();
        String description = payload.get("description") == null ? "" : String.valueOf(payload.get("description")).trim();
        if (categoryId == null || categoryId <= 0 || !StringUtils.hasText(name))
            throw new ApiException("商品信息不完整");

        if (id == null || id <= 0) {
            Food food = new Food();
            food.setCategoryId(categoryId); food.setName(name);
            food.setPrice(price); food.setImageUrl(imageUrl);
            food.setStatus(status); food.setDescription(description);
            food.setCreateTime(LocalDateTime.now());
            foodMapper.insert(food);
            Map<String, Object> res = new HashMap<>();
            res.put("msg", "添加成功"); res.put("id", food.getId());
            return res;
        }
        Food food = new Food();
        food.setId(id); food.setCategoryId(categoryId); food.setName(name);
        food.setPrice(price); food.setImageUrl(imageUrl);
        food.setStatus(status); food.setDescription(description);
        food.setUpdateTime(LocalDateTime.now());
        if (foodMapper.update(food) <= 0) throw new ApiException("商品记录不存在");
        return Map.of("msg", "保存成功");
    }

    /** 删除商品（recycle=true 物理删除 + 清理图片；false 软删除） */
    public Map<String, Object> delete(Integer id, boolean recycle) {
        if (id == null || id <= 0) throw new ApiException("未找到指定记录");
        if (recycle) {
            String imageUrl = foodMapper.selectImageUrlInRecycle(id);
            if (imageUrl == null) throw new ApiException("未找到指定记录");
            deleteImageFile(imageUrl);
            foodMapper.hardDelete(id);
        } else {
            if (foodMapper.softDelete(id, LocalDateTime.now()) <= 0)
                throw new ApiException("未找到指定记录");
        }
        return Map.of("msg", "删除成功");
    }

    /** 更新商品图片路径 */
    public void updateImageUrl(Integer id, String path) {
        if (id == null || id <= 0) throw new ApiException("未找到指定记录");
        foodMapper.updateImageUrl(id, path, LocalDateTime.now());
    }

    /** 创建草稿商品（先上传图片，再填写其他信息） */
    public Integer createDraftWithImage(String path) {
        Food food = new Food();
        food.setImageUrl(path);
        food.setCreateTime(LocalDateTime.now());
        foodMapper.insertDraft(food);
        return food.getId();
    }

    /** 清除商品图片（同时删除本地文件） */
    public void clearImageUrl(Integer id) {
        if (id == null || id <= 0) return;
        String oldPath = foodMapper.selectImageUrlById(id);
        foodMapper.clearImageUrl(id, LocalDateTime.now());
        deleteImageFile(oldPath);
    }

    /** 删除本地图片文件 */
    private void deleteImageFile(String relativePath) {
        if (!StringUtils.hasText(relativePath)) return;
        try { java.nio.file.Files.deleteIfExists(java.nio.file.Paths.get("./uploads", relativePath)); }
        catch (Exception ignored) {}
    }
}
