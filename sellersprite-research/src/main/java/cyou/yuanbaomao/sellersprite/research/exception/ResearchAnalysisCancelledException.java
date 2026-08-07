package cyou.yuanbaomao.sellersprite.research.exception;

/** 分析运行收到协作式取消请求。 */
public class ResearchAnalysisCancelledException extends RuntimeException {

    public ResearchAnalysisCancelledException(String analysisRunId) {
        super("市场调研分析已请求取消: " + analysisRunId);
    }
}
