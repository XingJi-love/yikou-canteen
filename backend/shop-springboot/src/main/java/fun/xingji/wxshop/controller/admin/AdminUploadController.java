package fun.xingji.wxshop.controller.admin;

import fun.xingji.wxshop.service.AdminUploadService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * 管理端文件上传 API
 * 上传图片文件（关联商品）、删除商品图片
 * 路径前缀: /admin/api/upload
 */
@RestController
@RequestMapping("/admin/api/upload")
public class AdminUploadController {

    private final AdminUploadService adminUploadService;

    public AdminUploadController(AdminUploadService adminUploadService) {
        this.adminUploadService = adminUploadService;
    }

    /**
     * POST - 上传图片文件
     */
    @PostMapping
    public Map<String, Object> upload(@RequestParam("file") MultipartFile file,
                                      @RequestParam(required = false) String relation,
                                      @RequestParam(name = "relation_id", required = false) Integer relationId) {
        return adminUploadService.upload(file, relation, relationId);
    }

    /**
     * POST - 从URL下载图片并保存
     */
    @PostMapping("/url")
    public Map<String, Object> uploadFromUrl(@RequestParam("url") String url,
                                             @RequestParam(required = false) String relation,
                                             @RequestParam(name = "relation_id", required = false) Integer relationId) {
        return adminUploadService.uploadFromUrl(url, relation, relationId);
    }

    /**
     * DELETE - 删除商品关联图片
     */
    @DeleteMapping
    public Map<String, Object> delete(@RequestParam(name = "relation_id") Integer relationId) {
        return adminUploadService.delete(relationId);
    }
}
