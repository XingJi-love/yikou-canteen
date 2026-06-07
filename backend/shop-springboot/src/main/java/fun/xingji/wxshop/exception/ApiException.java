package fun.xingji.wxshop.exception;

/**
 * 业务异常类
 * 用于在 Service 层抛出可预见的业务错误，由 GlobalExceptionHandler 统一拦截处理
 */
public class ApiException extends RuntimeException {

    private final int code;

    /**
     * @param message 错误提示信息，将直接返回给前端
     */
    public ApiException(String message) {
        super(message);
        this.code = 0;
    }

    /**
     * @param code    业务错误码，用于前端区分异常类型
     * @param message 错误提示信息，将直接返回给前端
     */
    public ApiException(int code, String message) {
        super(message);
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
