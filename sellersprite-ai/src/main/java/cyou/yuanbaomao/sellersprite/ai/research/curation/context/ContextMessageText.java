package cyou.yuanbaomao.sellersprite.ai.research.curation.context;

import java.util.stream.Collectors;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.ToolResponseMessage;

final class ContextMessageText {

    private ContextMessageText() {
    }

    static String extract(Message message) {
        if (message instanceof ToolResponseMessage toolResponseMessage) {
            return toolResponseMessage.getResponses().stream()
                    .map(response -> "工具 " + response.name() + " 返回：" + nullToEmpty(response.responseData()))
                    .collect(Collectors.joining("\n"));
        }
        return message.getText();
    }

    private static String nullToEmpty(String value) {
        return value == null ? "" : value;
    }
}
