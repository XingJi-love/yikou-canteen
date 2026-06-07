package fun.xingji.wxshop.util;

import java.math.BigDecimal;
import java.sql.Timestamp;

/**
 * 公共工具方法
 */
public final class CommonUtil {

    private CommonUtil() {
    }

    /**
     * 安全地将 Object 转为 Integer
     */
    public static Integer toInt(Object v) {
        if (v == null) {
            return null;
        }
        if (v instanceof Number n) {
            return n.intValue();
        }
        try {
            return Integer.parseInt(String.valueOf(v));
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 安全地将 Object 转为 BigDecimal
     */
    public static BigDecimal toDecimal(Object v) {
        if (v == null) {
            return BigDecimal.ZERO;
        }
        if (v instanceof BigDecimal d) {
            return d;
        }
        if (v instanceof Number n) {
            return BigDecimal.valueOf(n.doubleValue());
        }
        try {
            return new BigDecimal(String.valueOf(v));
        } catch (Exception e) {
            return BigDecimal.ZERO;
        }
    }

    /**
     * 将 Map 中 Timestamp 类型的值转为毫秒时间戳
     */
    public static void toTimestampMillis(java.util.Map<String, Object> map, String key) {
        Object v = map.get(key);
        if (v instanceof Timestamp ts) {
            map.put(key, ts.getTime());
        }
    }

    /**
     * 拼接完整 URL：如果 url 不是以 http 开头，则补上域名前缀
     */
    public static String urlFix(String domain, String url) {
        if (url == null || url.isBlank()) {
            return "";
        }
        if (url.startsWith("http://") || url.startsWith("https://")) {
            return url;
        }
        if (url.startsWith("/")) {
            return domain + url;
        }
        return domain + "/" + url;
    }
}
