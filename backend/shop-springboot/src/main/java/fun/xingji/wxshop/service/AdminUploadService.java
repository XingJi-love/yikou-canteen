package fun.xingji.wxshop.service;

import fun.xingji.wxshop.exception.ApiException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 管理端文件上传服务
 * 支持本地上传和 URL 下载两种方式
 * 根据 relation 自动路由到不同子目录（swiper/recommend/avatar/food）
 * 上传后自动关联商品（food）并更新图片路径或创建草稿
 */
@Service
public class AdminUploadService extends BaseService {

    private static final Set<String> ALLOWED_EXT = Set.of("gif", "jpg", "jpeg", "png", "bmp");
    private static final long MAX_SIZE = 1_000_000L; // 1MB

    @Value("${app.static-upload-dir}")
    private String staticUploadDir;

    private final AdminFoodService adminFoodService;

    public AdminUploadService(AdminFoodService adminFoodService) {
        this.adminFoodService = adminFoodService;
    }

    /** 上传文件（本地上传） */
    public Map<String, Object> upload(MultipartFile file, String relation, Integer relationId) {
        if (file == null || file.isEmpty()) throw new ApiException("请选择文件");
        if (file.getSize() > MAX_SIZE) throw new ApiException("上传文件大小超过限制");
        String ext = extension(file.getOriginalFilename());
        if (!ALLOWED_EXT.contains(ext)) throw new ApiException("上传文件扩展名不允许，仅支持 gif/jpg/jpeg/png/bmp");
        if (!StringUtils.hasText(staticUploadDir)) throw new ApiException("上传目录未配置");

        String subPath = resolveSubPath(relation);
        Path dir = Paths.get(staticUploadDir, subPath);
        try {
            Files.createDirectories(dir);
            String filename = UUID.randomUUID().toString().replace("-", "") + "." + ext;
            Path target = dir.resolve(filename);
            file.transferTo(target.toFile());
            String relativePath = subPath + filename;
            Integer foodId = handleFoodRelation(relation, relationId, relativePath);
            Map<String, Object> res = new HashMap<>();
            res.put("path", relativePath);
            res.put("url", "/static/uploads/" + relativePath);
            if (foodId != null) res.put("relation_id", foodId);
            return res;
        } catch (IOException e) { throw new ApiException("上传文件失败"); }
    }

    /** 删除与商品关联的图片 */
    public Map<String, Object> delete(Integer relationId) {
        if (relationId == null || relationId <= 0) throw new ApiException("未找到指定记录");
        adminFoodService.clearImageUrl(relationId);
        return Map.of("msg", "删除成功");
    }

    /** 从 URL 下载图片并保存到本地 */
    public Map<String, Object> uploadFromUrl(String imageUrl, String relation, Integer relationId) {
        if (!StringUtils.hasText(imageUrl) || !imageUrl.startsWith("http"))
            throw new ApiException("请输入有效的图片URL");
        String ext = extensionFromUrl(imageUrl);
        if (ext.isEmpty() || !ALLOWED_EXT.contains(ext)) ext = "jpg";
        String subPath = resolveSubPath(relation);
        Path dir = Paths.get(staticUploadDir, subPath);
        try {
            Files.createDirectories(dir);
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(imageUrl)).timeout(java.time.Duration.ofSeconds(30)).GET().build();
            HttpResponse<InputStream> response = client.send(request, HttpResponse.BodyHandlers.ofInputStream());
            if (response.statusCode() != 200)
                throw new ApiException("无法下载图片，HTTP状态码: " + response.statusCode());
            String filename = UUID.randomUUID().toString().replace("-", "") + "." + ext;
            Path target = dir.resolve(filename);
            try (InputStream is = response.body()) { Files.copy(is, target, StandardCopyOption.REPLACE_EXISTING); }
            String relativePath = subPath + filename;
            Integer foodId = handleFoodRelation(relation, relationId, relativePath);
            Map<String, Object> res = new HashMap<>();
            res.put("path", relativePath);
            res.put("url", "/static/uploads/" + relativePath);
            if (foodId != null) res.put("relation_id", foodId);
            return res;
        } catch (IOException | InterruptedException e) {
            throw new ApiException("从URL下载图片失败: " + e.getMessage());
        }
    }

    /** 如果 relation 为 food，则更新已存在商品的图片或创建草稿 */
    private Integer handleFoodRelation(String relation, Integer relationId, String relativePath) {
        if (!"food".equalsIgnoreCase(relation)) return relationId;
        if (relationId != null && relationId > 0) {
            adminFoodService.updateImageUrl(relationId, relativePath);
            return relationId;
        }
        return adminFoodService.createDraftWithImage(relativePath);
    }

    /** 根据 relation 路由到不同的上传子目录 */
    private String resolveSubPath(String relation) {
        if ("swiper".equalsIgnoreCase(relation)) return "settings/swiper/";
        if ("recommend".equalsIgnoreCase(relation)) return "settings/recommend/";
        if ("avatar".equalsIgnoreCase(relation)) return "avatars/";
        return "images/" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM/dd", Locale.ROOT)) + "/";
    }

    /** 从 URL 中提取文件扩展名 */
    private String extensionFromUrl(String url) {
        String path = url.split("\\?")[0];
        int dotIdx = path.lastIndexOf('.');
        if (dotIdx > 0 && dotIdx < path.length() - 1)
            return path.substring(dotIdx + 1).toLowerCase(Locale.ROOT);
        return "";
    }

    /** 从文件名提取扩展名 */
    private String extension(String filename) {
        if (!StringUtils.hasText(filename) || !filename.contains(".")) return "";
        return filename.substring(filename.lastIndexOf('.') + 1).toLowerCase(Locale.ROOT);
    }
}
