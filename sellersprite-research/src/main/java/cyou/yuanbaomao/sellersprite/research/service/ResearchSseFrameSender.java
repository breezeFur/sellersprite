package cyou.yuanbaomao.sellersprite.research.service;

import cyou.yuanbaomao.sellersprite.research.model.vo.ResearchStreamFrameVo;
import java.io.IOException;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/** 将聚合帧写入Spring MVC的SseEmitter。 */
@Component
public class ResearchSseFrameSender {

    public void send(SseEmitter emitter, ResearchStreamFrameVo frame) throws IOException {
        emitter.send(SseEmitter.event()
                .id(String.valueOf(frame.getLastSequence()))
                .name(frame.getFrameType())
                .data(frame));
    }

    public void heartbeat(SseEmitter emitter) throws IOException {
        emitter.send(SseEmitter.event().comment("heartbeat"));
    }
}
