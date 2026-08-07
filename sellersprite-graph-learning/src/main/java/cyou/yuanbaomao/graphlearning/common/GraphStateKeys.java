package cyou.yuanbaomao.graphlearning.common;

/**
 * 教学 Graph 使用的状态键，限定在通用示例边界内，不承载业务表字段。
 */
public final class GraphStateKeys {

    /** 原始输入文本或测试输入。 */
    public static final String INPUT = "input";
    /** 节点处理后的文本。 */
    public static final String PROCESSED = "processed";
    /** 最终输出。 */
    public static final String RESULT = "result";
    /** 当前分数。 */
    public static final String SCORE = "score";
    /** 已执行的修复或重试次数。 */
    public static final String ATTEMPT = "attempt";
    /** 条件边使用的路由决定。 */
    public static final String DECISION = "decision";
    /** 可追加的节点轨迹。 */
    public static final String TRACE = "trace";
    /** AI 分类结果。 */
    public static final String CLASSIFICATION = "classification";
    /** AI 置信度。 */
    public static final String CONFIDENCE = "confidence";
    /** 人工审核结果。 */
    public static final String APPROVED = "approved";

    private GraphStateKeys() {
    }
}
