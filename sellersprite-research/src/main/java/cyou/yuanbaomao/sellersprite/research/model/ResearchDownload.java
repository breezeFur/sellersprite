package cyou.yuanbaomao.sellersprite.research.model;

import org.springframework.core.io.Resource;

/**
 * 已完成完整性复核的报告下载对象。
 */
public record ResearchDownload(
        Resource resource,
        String fileName,
        String mediaType,
        long contentLength) {
}
