package fun.xingji.wxshop.util;

import org.springframework.util.DigestUtils;

import java.nio.charset.StandardCharsets;

/**
 * 管理员密码工具类
 * 使用 MD5 进行密码哈希与比对
 */
public final class AdminPassword {

    private AdminPassword() {
    }

    /**
     * 对明文密码进行 MD5 哈希
     *
     * @param password 明文密码
     * @return MD5 十六进制字符串
     */
    public static String hash(String password) {
        return DigestUtils.md5DigestAsHex(password.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 校验明文与哈希是否匹配
     *
     * @param rawPassword     明文密码
     * @param encodedPassword 已存储的 MD5 哈希
     * @return true=匹配
     */
    public static boolean matches(String rawPassword, String encodedPassword) {
        return hash(rawPassword).equalsIgnoreCase(encodedPassword);
    }
}
