package cyou.yuanbaomao.sellersprite.ai.research.curation.react;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AmazonSelectionReactEvent {

    private String eventType;

    private String conversationId;

    private Integer stepIndex;

    private String sheetName;

    private String phase;

    private String message;

    private Object data;
}
