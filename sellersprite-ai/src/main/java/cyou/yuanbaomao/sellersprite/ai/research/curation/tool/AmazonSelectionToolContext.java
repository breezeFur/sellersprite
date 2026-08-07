package cyou.yuanbaomao.sellersprite.ai.research.curation.tool;

import cyou.yuanbaomao.sellersprite.ai.research.curation.react.AmazonSelectionReactResult;
import cyou.yuanbaomao.sellersprite.ai.research.curation.model.ProductWorkbook;
import cyou.yuanbaomao.sellersprite.research.model.ResearchRawDataAccessScope;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.springframework.stereotype.Component;

@Component
public class AmazonSelectionToolContext {

    private final ConcurrentMap<String, AmazonSelectionReactResult> runs = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, ResearchRawDataAccessScope> rawDataScopes = new ConcurrentHashMap<>();

    public void start(String analysisRunId, ProductWorkbook workbook) {
        start(analysisRunId, analysisRunId, workbook);
    }

    public void start(String analysisRunId, String conversationId, ProductWorkbook workbook) {
        start(analysisRunId, conversationId, workbook, null);
    }

    public void start(
            String analysisRunId,
            String conversationId,
            ProductWorkbook workbook,
            ResearchRawDataAccessScope rawDataScope) {
        AmazonSelectionReactResult result = new AmazonSelectionReactResult();
        result.setConversationId(conversationId);
        result.setWorkbook(workbook);
        AmazonSelectionReactResult existing = runs.putIfAbsent(analysisRunId, result);
        if (existing != null) {
            throw new IllegalStateException("分析运行上下文已存在：" + analysisRunId);
        }
        if (rawDataScope != null) {
            rawDataScopes.put(analysisRunId, rawDataScope);
        }
    }

    public Optional<AmazonSelectionReactResult> get(String analysisRunId) {
        return Optional.ofNullable(runs.get(analysisRunId));
    }

    public AmazonSelectionReactResult getRequired(String analysisRunId) {
        AmazonSelectionReactResult result = runs.get(analysisRunId);
        if (result == null) {
            throw new IllegalStateException("未找到分析运行上下文：" + analysisRunId);
        }
        return result;
    }

    public AmazonSelectionReactResult finish(String analysisRunId) {
        return getRequired(analysisRunId);
    }

    public ResearchRawDataAccessScope getRequiredRawDataScope(String analysisRunId) {
        ResearchRawDataAccessScope scope = rawDataScopes.get(analysisRunId);
        if (scope == null) {
            throw new IllegalStateException("当前分析运行未绑定原始数据访问范围：" + analysisRunId);
        }
        return scope;
    }

    public void remove(String analysisRunId) {
        runs.remove(analysisRunId);
        rawDataScopes.remove(analysisRunId);
    }
}
