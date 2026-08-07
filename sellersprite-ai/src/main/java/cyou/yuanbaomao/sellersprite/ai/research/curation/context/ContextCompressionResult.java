package cyou.yuanbaomao.sellersprite.ai.research.curation.context;

import java.util.List;
import org.springframework.ai.chat.messages.Message;

public record ContextCompressionResult(
        List<Message> compressedMessages,
        boolean modelInvoked) {
}
