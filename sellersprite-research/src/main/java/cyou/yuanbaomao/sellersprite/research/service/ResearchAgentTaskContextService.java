package cyou.yuanbaomao.sellersprite.research.service;

import cyou.yuanbaomao.sellersprite.db.dao.MarketResearchJobDao;
import cyou.yuanbaomao.sellersprite.db.entity.MarketResearchJob;
import cyou.yuanbaomao.sellersprite.research.enums.ResearchStageCode;
import cyou.yuanbaomao.sellersprite.research.model.CollectionGraphConfig;
import cyou.yuanbaomao.sellersprite.research.model.ResearchInput;
import cyou.yuanbaomao.sellersprite.research.model.ResearchProductSelection;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import tools.jackson.databind.ObjectMapper;

/** 将任务创建时固化的采集参数整理为 Agent 可理解的事实上下文。 */
@Service
@RequiredArgsConstructor
public class ResearchAgentTaskContextService {

    private static final Map<String, String> REVIEW_STAR_LABELS = Map.ofEntries(
            Map.entry("1", "1星"),
            Map.entry("2", "2星"),
            Map.entry("3", "3星"),
            Map.entry("4", "4星"),
            Map.entry("5", "5星"),
            Map.entry("one_star", "1星"),
            Map.entry("two_star", "2星"),
            Map.entry("three_star", "3星"),
            Map.entry("four_star", "4星"),
            Map.entry("five_star", "5星"));
    private static final Map<String, String> REVIEW_TYPE_LABELS = Map.ofEntries(
            Map.entry("1", "图片评论"),
            Map.entry("2", "视频评论"),
            Map.entry("3", "VP评论"),
            Map.entry("4", "Vine评论"),
            Map.entry("verified_purchase", "VP评论"));

    private final MarketResearchJobDao jobDao;
    private final ResearchInputService inputService;
    private final ResearchStageInputService stageInputService;
    private final ObjectMapper objectMapper;

    public String describe(String jobId, ResearchStageCode stageCode) {
        MarketResearchJob job = jobDao.getById(jobId);
        if (job == null) {
            throw new IllegalStateException("市场调研任务不存在: " + jobId);
        }
        ResearchInput input = inputService.from(job);
        CollectionGraphConfig.ReviewCollectionConfig reviewConfig =
                input.getCollectionConfig().getCollectReviews();
        List<String> selectedAsins = stageInputService.findSelection(jobId)
                .map(ResearchProductSelection::selectedAsins)
                .orElse(input.getSeedAsins());
        List<String> starList = reviewConfig.getStarList() == null
                ? List.of()
                : reviewConfig.getStarList();
        List<String> typeList = reviewConfig.getTypeList() == null
                ? List.of()
                : reviewConfig.getTypeList();
        String reviewBoundary = starList.isEmpty()
                ? "评论来自分页抽样，并非全部评论；可以分析样本主题，但不得把样本分布当作总体精确分布。"
                : "评论是所选星级的定向样本；只能分析这些星级样本中的需求和问题，"
                        + "不得据此推断总体差评率、总体平均星级或总体满意度。";

        return """
                【本次任务采集输入参数】
                以下内容是创建任务时持久化的采集范围，不是分析结果。
                - 当前阶段：%s
                - marketplace：%s
                - nodeIdPath：%s
                - researchMonth：%s
                - keyword：%s
                - 人工选中/种子 ASIN：%s
                - 完整采集配置 JSON：%s

                【评论样本口径】
                - 星级筛选：%s
                - 类型筛选：%s
                - 每个 ASIN 目标评论数：%s
                - 分析限制：%s
                """.formatted(
                stageCode.name(),
                display(input.getMarketplace()),
                display(input.getNodeIdPath()),
                display(input.getMonth()),
                display(input.getKeyword()),
                selectedAsins == null || selectedAsins.isEmpty() ? "未选择" : selectedAsins,
                writeConfig(input.getCollectionConfig()),
                labels(starList, REVIEW_STAR_LABELS, "全部星级（未设置筛选）"),
                labels(typeList, REVIEW_TYPE_LABELS, "全部类型（未设置筛选）"),
                reviewConfig.getPagination().getTargetCountPerAsin(),
                reviewBoundary);
    }

    private String writeConfig(CollectionGraphConfig config) {
        try {
            return objectMapper.writeValueAsString(config);
        } catch (Exception exception) {
            throw new IllegalStateException("序列化任务采集配置失败", exception);
        }
    }

    private String labels(List<String> values, Map<String, String> labels, String emptyLabel) {
        if (values.isEmpty()) {
            return emptyLabel;
        }
        return values.stream()
                .map(value -> labels.getOrDefault(value, value))
                .toList()
                .toString();
    }

    private String display(String value) {
        return StringUtils.hasText(value) ? value : "未设置";
    }
}
