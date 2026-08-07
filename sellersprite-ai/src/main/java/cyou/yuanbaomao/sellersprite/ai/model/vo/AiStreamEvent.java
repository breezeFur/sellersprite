package cyou.yuanbaomao.sellersprite.ai.model.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AiStreamEvent {
    private String event;
    private Object data;
}
