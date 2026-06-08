package fun.xingji.wxshop.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.util.Map;

/**
 * 全局异常处理器
 * 统一拦截 Controller 抛出的各类异常，返回规范的 JSON 错误响应
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // ==================== 业务层异常 ====================

    /**
     * 处理业务异常 ApiException
     * WARN 级别日志，将异常消息和错误码返回前端
     */
    @ExceptionHandler(ApiException.class)
    public Map<String, Object> handleApiException(ApiException ex) {
        log.warn("业务异常 [code={}]: {}", ex.getCode(), ex.getMessage());
        return Map.of("code", ex.getCode(), "msg", ex.getMessage());
    }

    // ==================== 请求参数校验异常 ====================

    /**
     * 处理 @Valid 参数校验失败（如 @NotNull、@NotBlank 等注解校验不通过）
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Map<String, Object> handleValidationException(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .reduce((a, b) -> a + "; " + b)
                .orElse("参数校验失败");
        log.warn("参数校验失败: {}", message);
        return Map.of("code", 0, "msg", message);
    }

    /**
     * 处理请求参数类型转换失败（如传字符串给 Integer 参数）
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public Map<String, Object> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        String name = ex.getName();
        String requiredType = ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "";
        String msg = String.format("参数'%s'类型错误，期望: %s", name, requiredType);
        log.warn(msg);
        return Map.of("code", 0, "msg", msg);
    }

    /**
     * 处理必填请求参数缺失
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public Map<String, Object> handleMissingParam(MissingServletRequestParameterException ex) {
        String msg = String.format("缺少必填参数: %s (%s)", ex.getParameterName(), ex.getParameterType());
        log.warn(msg);
        return Map.of("code", 0, "msg", msg);
    }

    /**
     * 处理请求体解析失败（如 JSON 格式错误、缺少必填字段）
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public Map<String, Object> handleBadRequest(HttpMessageNotReadableException ex) {
        String causeMsg = ex.getCause() != null ? ex.getCause().getMessage() : null;
        if (causeMsg != null && causeMsg.contains("Required request body is missing")) {
            log.warn("请求体为空");
            return Map.of("code", 0, "msg", "请求体不能为空");
        }
        log.warn("请求参数解析失败: {}", ex.getMessage());
        return Map.of("code", 0, "msg", "请求参数格式错误");
    }

    // ==================== 文件上传异常 ====================

    /**
     * 处理文件上传大小超限
     */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public Map<String, Object> handleMaxUploadSize(MaxUploadSizeExceededException ex) {
        long maxSizeBytes = ex.getMaxUploadSize();
        double maxSizeMB = maxSizeBytes / (1024.0 * 1024.0);
        log.warn("文件上传超限: {}MB", maxSizeMB);
        return Map.of("code", 0, "msg", "上传文件超过" + String.format("%.1f", maxSizeMB) + "MB限制");
    }

    // ==================== AI 服务异常 ====================

    /**
     * 处理 AI 服务调用失败
     */
    @ExceptionHandler(fun.xingji.wxshop.ai.service.AiService.AiServiceException.class)
    public Map<String, Object> handleAiServiceException(fun.xingji.wxshop.ai.service.AiService.AiServiceException ex) {
        log.warn("AI 服务异常: {}", ex.getMessage());
        return Map.of("code", 0, "msg", ex.getMessage());
    }

    // ==================== 数据访问异常 ====================

    /**
     * 处理数据库操作异常（SQL 语法/连接超时等）
     * 隐藏敏感信息，仅记录完整堆栈到日志
     */
    @ExceptionHandler(DataAccessException.class)
    public Map<String, Object> handleDataAccessException(DataAccessException ex) {
        log.error("数据操作异常: {}", ex.getMessage(), ex);
        return Map.of("code", 0, "msg", "数据操作失败，请稍后重试");
    }

    // ==================== 兜底异常 ====================

    /**
     * 处理所有未捕获的异常（兜底）
     * ERROR 级别日志，返回通用服务器错误信息
     */
    @ExceptionHandler(Exception.class)
    public Map<String, Object> handleOther(Exception ex) {
        log.error("未预期异常: [{}] {}", ex.getClass().getSimpleName(), ex.getMessage(), ex);
        return Map.of("code", 0, "msg", "服务器异常，请稍后重试");
    }
}
