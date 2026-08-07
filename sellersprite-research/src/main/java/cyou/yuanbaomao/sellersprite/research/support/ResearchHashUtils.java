package cyou.yuanbaomao.sellersprite.research.support;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

/**
 * 数据集与报告文件摘要工具。
 */
public final class ResearchHashUtils {

    private static final int BUFFER_SIZE = 8192;

    private ResearchHashUtils() {
    }

    public static String sha256(String value) {
        MessageDigest digest = sha256Digest();
        return HexFormat.of().formatHex(digest.digest(value.getBytes(StandardCharsets.UTF_8)));
    }

    public static String sha256(byte[] value) {
        MessageDigest digest = sha256Digest();
        return HexFormat.of().formatHex(digest.digest(value));
    }

    public static String sha256(Path path) throws IOException {
        try (InputStream inputStream = Files.newInputStream(path)) {
            return sha256(inputStream);
        }
    }

    public static String sha256(InputStream inputStream) throws IOException {
        MessageDigest digest = sha256Digest();
        byte[] buffer = new byte[BUFFER_SIZE];
        int read;
        while ((read = inputStream.read(buffer)) != -1) {
            if (read > 0) {
                digest.update(buffer, 0, read);
            }
        }
        return HexFormat.of().formatHex(digest.digest());
    }

    private static MessageDigest sha256Digest() {
        try {
            return MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("当前JDK不支持SHA-256", exception);
        }
    }
}
