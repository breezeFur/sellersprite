package cyou.yuanbaomao.sellersprite.ai.research.curation.context;

public record ContextWindowSnapshot(
        int estimatedTokens,
        int maxContextTokens,
        int thresholdTokens,
        double triggerRatio,
        boolean requiresCompression) {
}
