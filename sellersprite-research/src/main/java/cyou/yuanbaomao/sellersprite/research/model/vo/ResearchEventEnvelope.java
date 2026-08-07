package cyou.yuanbaomao.sellersprite.research.model.vo;

import cyou.yuanbaomao.sellersprite.research.event.ResearchEventScope;
import lombok.Builder;
import lombok.Value;

/** SSE 与历史事件查询共用的稳定事件信封。 */
@Value
@Builder
public class ResearchEventEnvelope {

    Long sequenceNo;
    String eventId;
    String jobId;
    String conversationId;
    String analysisRunId;
    ResearchEventScope scope;
    String eventType;
    String phase;
    String sheetName;
    String nodeCode;
    String message;
    Object payload;
    boolean terminal;
    Long createdAt;
}
