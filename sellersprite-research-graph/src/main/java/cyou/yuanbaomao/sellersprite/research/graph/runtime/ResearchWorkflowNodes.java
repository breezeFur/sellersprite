package cyou.yuanbaomao.sellersprite.research.graph.runtime;

import com.alibaba.cloud.ai.graph.OverAllState;
import cyou.yuanbaomao.sellersprite.research.enums.ResearchPhase;
import cyou.yuanbaomao.sellersprite.research.enums.ResearchSelectionDecision;
import cyou.yuanbaomao.sellersprite.research.enums.ResearchStageCode;
import cyou.yuanbaomao.sellersprite.research.model.ResearchExecutionLease;
import cyou.yuanbaomao.sellersprite.research.model.ResearchProductSelection;
import cyou.yuanbaomao.sellersprite.research.service.ResearchArtifactFinalizationPort;
import cyou.yuanbaomao.sellersprite.research.service.ResearchJobStateService;
import cyou.yuanbaomao.sellersprite.research.service.ResearchStageAnalysisPort;
import cyou.yuanbaomao.sellersprite.research.service.ResearchStageInputService;
import cyou.yuanbaomao.sellersprite.research.service.ResearchWorkflowStepService;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/** 将Graph状态适配到与框架无关的Research业务节点服务。 */
@Component
@RequiredArgsConstructor
public class ResearchWorkflowNodes {

    public static final String STATE_JOB_ID = "jobId";
    public static final String STATE_WORKFLOW_VERSION = "workflowVersion";
    public static final String STATE_EXECUTION_OWNER = "executionOwner";
    public static final String STATE_EXECUTION_TOKEN = "executionToken";
    public static final String STATE_ATTEMPT_COUNT = "attemptCount";
    public static final String STATE_LAST_GRAPH = "lastGraph";
    public static final String STATE_LAST_NODE = "lastNode";
    public static final String STATE_SELECTION_DECISION = "selectionDecision";
    public static final String STATE_WORKFLOW_OUTCOME = "workflowOutcome";

    public static final String OUTCOME_SUCCEEDED = "SUCCEEDED";
    public static final String OUTCOME_ABANDONED = "ABANDONED";

    private final ResearchWorkflowStepService stepService;
    private final ResearchStageInputService stageInputService;
    private final ResearchStageAnalysisPort stageAnalysisPort;
    private final ResearchArtifactFinalizationPort artifactFinalizationPort;
    private final ResearchJobStateService jobStateService;

    public Map<String, Object> execute(OverAllState state, ResearchPhase phase) {
        String jobId = state.value(STATE_JOB_ID, String.class)
                .filter(StringUtils::hasText)
                .orElseThrow(() -> new IllegalStateException("Graph状态缺少jobId"));
        String executionToken = state.value(STATE_EXECUTION_TOKEN, String.class)
                .filter(StringUtils::hasText)
                .orElseThrow(() -> new IllegalStateException("Graph状态缺少executionToken"));
        stepService.execute(jobId, executionToken, phase);
        return Map.of(
                STATE_LAST_GRAPH, phase.getGraphCode().getCode(),
                STATE_LAST_NODE, phase.getNodeCode());
    }

    public Map<String, Object> enterStage(OverAllState state, ResearchStageCode stage) {
        jobStateService.moveToStage(executionLease(state), stage);
        return Map.of(
                STATE_LAST_GRAPH, stage.name(),
                STATE_LAST_NODE, stage.name() + ".enter");
    }

    public Map<String, Object> runStageAnalysis(OverAllState state, ResearchStageCode stage) {
        String jobId = requiredText(state, STATE_JOB_ID);
        String executionToken = requiredText(state, STATE_EXECUTION_TOKEN);
        stageAnalysisPort.runStage(jobId, executionToken, stage);
        return Map.of(
                STATE_LAST_GRAPH, stage.name(),
                STATE_LAST_NODE, stage.name() + ".analysis");
    }

    public Map<String, Object> productSelectionGate(OverAllState state) {
        String jobId = requiredText(state, STATE_JOB_ID);
        ResearchProductSelection selection = stageInputService.findSelection(jobId)
                .orElseThrow(() -> new IllegalStateException("商品选择关卡缺少用户决定: " + jobId));
        return Map.of(
                STATE_SELECTION_DECISION, selection.decision().name(),
                STATE_LAST_GRAPH, ResearchStageCode.SCREENING.name(),
                STATE_LAST_NODE, "productSelectionGate");
    }

    public String selectionRoute(OverAllState state) {
        return requiredText(state, STATE_SELECTION_DECISION);
    }

    public Map<String, Object> finalizeArtifacts(OverAllState state) {
        String jobId = requiredText(state, STATE_JOB_ID);
        String executionToken = requiredText(state, STATE_EXECUTION_TOKEN);
        ResearchSelectionDecision decision = ResearchSelectionDecision.valueOf(
                requiredText(state, STATE_SELECTION_DECISION));
        artifactFinalizationPort.finalizeArtifacts(jobId, executionToken, decision);
        return Map.of(
                STATE_WORKFLOW_OUTCOME,
                decision == ResearchSelectionDecision.ABANDON
                        ? OUTCOME_ABANDONED
                        : OUTCOME_SUCCEEDED,
                STATE_LAST_NODE, "finalizeArtifacts");
    }

    private ResearchExecutionLease executionLease(OverAllState state) {
        return new ResearchExecutionLease(
                requiredText(state, STATE_JOB_ID),
                requiredText(state, STATE_WORKFLOW_VERSION),
                requiredText(state, STATE_EXECUTION_OWNER),
                requiredText(state, STATE_EXECUTION_TOKEN),
                requiredInt(state, STATE_ATTEMPT_COUNT));
    }

    private String requiredText(OverAllState state, String key) {
        return state.value(key, String.class)
                .filter(StringUtils::hasText)
                .orElseThrow(() -> new IllegalStateException("Graph状态缺少" + key));
    }

    private int requiredInt(OverAllState state, String key) {
        return state.value(key, Integer.class)
                .orElseThrow(() -> new IllegalStateException("Graph状态缺少" + key));
    }
}
