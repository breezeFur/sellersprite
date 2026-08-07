package cyou.yuanbaomao.sellersprite.research.graph.config;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

/** 为 H2 的 MySQL 兼容测试补充官方 MysqlSaver 使用的两个 JSON 函数。 */
public final class MysqlJsonFunctions {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private MysqlJsonFunctions() {
    }

    public static String jsonExtract(String json, String path) throws Exception {
        if (!"$.binaryPayload".equals(path)) {
            throw new IllegalArgumentException("Unsupported JSON path: " + path);
        }
        JsonNode root = OBJECT_MAPPER.readTree(json);
        if (root.isTextual()) {
            root = OBJECT_MAPPER.readTree(root.asText());
        }
        JsonNode value = root.get("binaryPayload");
        return value == null ? null : value.toString();
    }

    public static String jsonUnquote(String value) throws Exception {
        return value == null ? null : OBJECT_MAPPER.readTree(value).asText();
    }
}
