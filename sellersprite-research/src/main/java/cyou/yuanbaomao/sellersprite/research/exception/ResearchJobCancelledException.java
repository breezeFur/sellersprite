package cyou.yuanbaomao.sellersprite.research.exception;

/** 在安全节点边界检测到取消请求时终止Graph执行。 */
public class ResearchJobCancelledException extends RuntimeException {

    public ResearchJobCancelledException(String jobId) {
        super("市场调研任务已请求取消: " + jobId);
    }
}
