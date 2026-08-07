package cyou.yuanbaomao.sellersprite.ai.controller;

import cyou.yuanbaomao.base.result.Result;
import cyou.yuanbaomao.sellersprite.ai.model.dto.AiChatRequest;
import cyou.yuanbaomao.sellersprite.ai.model.vo.AiChatVo;
import cyou.yuanbaomao.sellersprite.ai.model.vo.AiStreamEvent;
import cyou.yuanbaomao.sellersprite.ai.service.AiChatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@Tag(name = "AI 接口", description = "大模型聊天接口")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/ai")
public class AiChatController {

    private final AiChatService aiChatService;

    @Operation(summary = "发送聊天消息", description = "调用 OpenAI 兼容聊天模型生成文本回复")
    @PostMapping("/chat")
    public Result<AiChatVo> chat(@Valid @RequestBody AiChatRequest request) {
        return Result.success(aiChatService.chat(request));
    }

    @Operation(summary = "流式发送聊天消息")
    @PostMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<Object>> stream(@Valid @RequestBody AiChatRequest request) {
        return aiChatService.stream(request)
                .map(event -> ServerSentEvent.builder(event.getData()).event(event.getEvent()).build());
    }

    @Operation(summary = "重试最后一条失败或已取消的助手回复")
    @PostMapping(value = "/conversations/{conversationId}/messages/{messageId}/retry",
            produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<Object>> retry(@PathVariable String conversationId,
                                                @PathVariable String messageId) {
        return aiChatService.retry(conversationId, messageId)
                .map(event -> ServerSentEvent.builder(event.getData()).event(event.getEvent()).build());
    }
}
