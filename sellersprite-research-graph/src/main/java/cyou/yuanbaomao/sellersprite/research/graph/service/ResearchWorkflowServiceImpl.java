package cyou.yuanbaomao.sellersprite.research.graph.service;

import com.alibaba.cloud.ai.graph.CompiledGraph;
import com.alibaba.cloud.ai.graph.GraphRepresentation;
import cyou.yuanbaomao.sellersprite.research.enums.ResearchStageCode;
import cyou.yuanbaomao.sellersprite.research.graph.config.ResearchGraphConfiguration;
import cyou.yuanbaomao.sellersprite.research.model.vo.ResearchWorkflowStepVo;
import cyou.yuanbaomao.sellersprite.research.model.vo.ResearchWorkflowTopologyVo;
import cyou.yuanbaomao.sellersprite.research.service.ResearchWorkflowService;
import java.util.List;
import org.springframework.stereotype.Service;

/** 从实际编译Graph导出拓扑，前端不维护第二套边定义。 */
@Service
public class ResearchWorkflowServiceImpl implements ResearchWorkflowService {

    private static final String WORKFLOW_TITLE = "市场调研工作流";
    private static final String PRODUCT_SELECTION_STEP_CODE = "PRODUCT_SELECTION";
    private static final String ARTIFACT_FINALIZATION_STEP_CODE = "ARTIFACT_FINALIZATION";
    private static final List<ResearchWorkflowStepVo> WORKFLOW_STEPS = List.of(
            step(
                    ResearchStageCode.SCREENING.name(),
                    ResearchGraphConfiguration.SCREENING_GRAPH_NODE,
                    "阶段一：市场初筛"),
            step(
                    PRODUCT_SELECTION_STEP_CODE,
                    ResearchGraphConfiguration.PRODUCT_SELECTION_GATE_NODE,
                    "商品选择"),
            step(
                    ResearchStageCode.DEEP_DIVE.name(),
                    ResearchGraphConfiguration.DEEP_DIVE_GRAPH_NODE,
                    "阶段二：商品深挖"),
            step(
                    ResearchStageCode.FINAL_ANALYSIS.name(),
                    ResearchGraphConfiguration.FINAL_ANALYSIS_GRAPH_NODE,
                    "阶段三：最终分析"),
            step(
                    ARTIFACT_FINALIZATION_STEP_CODE,
                    ResearchGraphConfiguration.FINALIZE_ARTIFACTS_NODE,
                    "生成并发布产物"));

    private final CompiledGraph graph;

    public ResearchWorkflowServiceImpl(CompiledGraph marketResearchGraph) {
        this.graph = marketResearchGraph;
    }

    @Override
    public ResearchWorkflowTopologyVo topology() {
        GraphRepresentation representation = graph.getGraph(
                GraphRepresentation.Type.MERMAID, WORKFLOW_TITLE);
        if (representation.content() == null || representation.content().isBlank()) {
            throw new IllegalStateException("市场调研Graph未生成Mermaid拓扑");
        }
        return ResearchWorkflowTopologyVo.builder()
                .type(representation.type().name())
                .title(WORKFLOW_TITLE)
                .content(representation.content())
                .steps(WORKFLOW_STEPS)
                .build();
    }

    private static ResearchWorkflowStepVo step(String code, String nodeCode, String label) {
        return ResearchWorkflowStepVo.builder()
                .code(code)
                .nodeCode(nodeCode)
                .label(label)
                .build();
    }
}
