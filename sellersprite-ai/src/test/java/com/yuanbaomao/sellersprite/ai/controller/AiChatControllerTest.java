package com.yuanbaomao.sellersprite.ai.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.yuanbaomao.sellersprite.ai.model.dto.AiChatRequest;
import com.yuanbaomao.sellersprite.ai.model.vo.AiStreamEvent;
import com.yuanbaomao.sellersprite.ai.service.AiChatService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.http.codec.ServerSentEvent;
import reactor.core.publisher.Flux;

class AiChatControllerTest {

    @Test
    void shouldMapDomainEventsToNamedServerSentEvents() {
        AiChatService service = mock(AiChatService.class);
        AiChatRequest request = new AiChatRequest();
        request.setPrompt("你好");
        when(service.stream(request)).thenReturn(Flux.just(
                new AiStreamEvent("conversation", "c"),
                new AiStreamEvent("delta", "d"),
                new AiStreamEvent("done", "x")));

        List<ServerSentEvent<Object>> events = new AiChatController(service).stream(request)
                .collectList().block();

        assertThat(events).extracting(ServerSentEvent::event)
                .containsExactly("conversation", "delta", "done");
    }

    @Test
    void shouldExposeFailedMessageRetryAsNamedServerSentEvents() {
        AiChatService service = mock(AiChatService.class);
        when(service.retry("conversation-1", "message-2")).thenReturn(Flux.just(
                new AiStreamEvent("conversation", "c"),
                new AiStreamEvent("delta", "d"),
                new AiStreamEvent("done", "x")));

        List<ServerSentEvent<Object>> events = new AiChatController(service)
                .retry("conversation-1", "message-2")
                .collectList().block();

        assertThat(events).extracting(ServerSentEvent::event)
                .containsExactly("conversation", "delta", "done");
    }
}
