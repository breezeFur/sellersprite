package cyou.yuanbaomao.sellersprite.research.controller;

import cyou.yuanbaomao.base.exception.BizException;
import cyou.yuanbaomao.sellersprite.common.result.ResultCode;
import cyou.yuanbaomao.sellersprite.research.service.ResearchEventStreamService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Tag(name = "市场调研事件流")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/market-research/jobs")
public class ResearchEventController {

    private static final String LAST_EVENT_ID_HEADER = "Last-Event-ID";

    private final ResearchEventStreamService eventStreamService;

    @Operation(summary = "订阅主动推送且可断点恢复的市场调研SSE聚合帧")
    @GetMapping(value = "/{jobId}/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter stream(
            @PathVariable String jobId,
            @RequestParam(defaultValue = "0") long afterSequence,
            @RequestHeader(value = LAST_EVENT_ID_HEADER, required = false) String lastEventId) {
        return eventStreamService.stream(jobId, Math.max(afterSequence, parseLastEventId(lastEventId)));
    }

    private long parseLastEventId(String lastEventId) {
        if (lastEventId == null || lastEventId.isBlank()) {
            return 0L;
        }
        try {
            return Math.max(0L, Long.parseLong(lastEventId.trim()));
        } catch (NumberFormatException exception) {
            throw new BizException(ResultCode.PARAM_INVALID, "Last-Event-ID必须是非负整数");
        }
    }
}
