package cyou.yuanbaomao.sellersprite.research.service;

import cyou.yuanbaomao.sellersprite.api.client.SellerSpriteOperation;
import cyou.yuanbaomao.sellersprite.api.common.enums.SellerSpriteMarketplace;
import cyou.yuanbaomao.sellersprite.api.product.model.dto.ProductNodeRequest;
import cyou.yuanbaomao.sellersprite.api.product.model.vo.ProductNodeVo;
import cyou.yuanbaomao.sellersprite.api.product.service.ProductService;
import cyou.yuanbaomao.sellersprite.research.cache.ResearchSourceCacheStore.CachedPayload;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import cyou.yuanbaomao.sellersprite.api.product.model.dto.CompetitorLookupRequest;
import cyou.yuanbaomao.sellersprite.api.common.model.vo.ProductSummaryVo;
import cyou.yuanbaomao.sellersprite.api.product.model.vo.CompetitorLookupVo;
import cyou.yuanbaomao.sellersprite.research.model.dto.CategoryResolveByAsinsRequest;
import cyou.yuanbaomao.sellersprite.research.model.vo.ResearchCategoryCandidateVo;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/** 为市场调研入口提供跨任务复用的产品类目查询与 ASIN 反查聚合服务。 */
@Service
@RequiredArgsConstructor
public class ResearchCategoryService {

    private static final TypeReference<List<ProductNodeVo>> PRODUCT_NODE_LIST_TYPE =
            new TypeReference<>() {
            };

    private final ProductService productService;
    private final ResearchSourceCacheService sourceCacheService;
    private final ObjectMapper objectMapper;

    public List<ProductNodeVo> listProductNodes(SellerSpriteMarketplace marketplace, String nodeIdPath,
            String keyword, String month) {
        ProductNodeRequest request = new ProductNodeRequest();
        request.setMarketplace(marketplace);
        request.setNodeIdPath(nodeIdPath);
        request.setKeyword(keyword);
        request.setMonth(month);
        CachedPayload cached = sourceCacheService.getOrLoad(
                SellerSpriteOperation.PRODUCT_NODE,
                request,
                sourceCacheService.productNodePolicy(),
                () -> objectMapper.valueToTree(productService.listProductNodes(
                        marketplace, nodeIdPath, keyword, month)));
        List<ProductNodeVo> nodes = objectMapper.convertValue(cached.payload(), PRODUCT_NODE_LIST_TYPE);
        return nodes == null ? List.of() : List.copyOf(nodes);
    }

    public List<ResearchCategoryCandidateVo> resolveCategoriesByAsins(CategoryResolveByAsinsRequest request) {
        if (request == null || request.getAsins() == null || request.getAsins().isEmpty()) {
            return List.of();
        }
        List<String> distinctAsins = request.getAsins().stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .distinct()
                .toList();
        if (distinctAsins.isEmpty()) {
            return List.of();
        }

        String month = normalizeMonth(request.getMonth());
        CompetitorLookupRequest competitorRequest = new CompetitorLookupRequest();
        competitorRequest.setMarketplace(request.getMarketplace());
        competitorRequest.setAsins(distinctAsins);
        competitorRequest.setMonth(month);

        CachedPayload cached = sourceCacheService.getOrLoad(
                SellerSpriteOperation.PRODUCT_COMPETITOR_LOOKUP,
                competitorRequest,
                sourceCacheService.asinPolicy(),
                () -> objectMapper.valueToTree(productService.lookupCompetitors(competitorRequest)));
        CompetitorLookupVo lookupResult = objectMapper.convertValue(cached.payload(), CompetitorLookupVo.class);
        if (lookupResult == null || lookupResult.getItems() == null || lookupResult.getItems().isEmpty()) {
            return List.of();
        }

        Map<String, List<ProductSummaryVo>> grouped = lookupResult.getItems().stream()
                .filter(item -> item.getNodeIdPath() != null && !item.getNodeIdPath().isBlank())
                .collect(Collectors.groupingBy(ProductSummaryVo::getNodeIdPath, LinkedHashMap::new, Collectors.toList()));

        int totalInputAsins = distinctAsins.size();
        List<ResearchCategoryCandidateVo> candidates = new ArrayList<>();

        for (Map.Entry<String, List<ProductSummaryVo>> entry : grouped.entrySet()) {
            String nodeIdPath = entry.getKey();
            List<ProductSummaryVo> items = entry.getValue();
            ProductSummaryVo sample = items.getFirst();

            String nodeLabelPath = sample.getNodeLabelPath();
            String nodeId = sample.getNodeId() != null ? String.valueOf(sample.getNodeId()) : extractLastSegment(nodeIdPath);
            String nodeLabel = extractLastSegment(nodeLabelPath);
            String displayName = nodeLabel != null && !nodeLabel.isBlank() ? nodeLabel : nodeIdPath;

            List<String> matchedAsins = items.stream()
                    .map(ProductSummaryVo::getAsin)
                    .filter(Objects::nonNull)
                    .distinct()
                    .toList();
            int matchedCount = matchedAsins.size();
            double matchedRatio = totalInputAsins > 0
                    ? Math.round((matchedCount * 100.0 / totalInputAsins) * 10.0) / 10.0
                    : 0.0;

            candidates.add(ResearchCategoryCandidateVo.builder()
                    .nodeIdPath(nodeIdPath)
                    .nodeId(nodeId)
                    .nodeLabelPath(nodeLabelPath)
                    .nodeLabel(nodeLabel)
                    .displayName(displayName)
                    .matchedCount(matchedCount)
                    .matchedAsins(matchedAsins)
                    .matchedRatio(matchedRatio)
                    .build());
        }

        candidates.sort(Comparator
                .comparing(ResearchCategoryCandidateVo::getMatchedCount, Comparator.reverseOrder())
                .thenComparing(ResearchCategoryCandidateVo::getNodeLabelPath, Comparator.nullsLast(String::compareTo)));

        return candidates;
    }

    private static String extractLastSegment(String path) {
        if (path == null || path.isBlank()) {
            return null;
        }
        int lastIndex = path.lastIndexOf(':');
        return lastIndex >= 0 ? path.substring(lastIndex + 1).trim() : path.trim();
    }

    private static String normalizeMonth(String month) {
        if (month == null || month.isBlank()) {
            return null;
        }
        return month.replace("-", "").trim();
    }
}
