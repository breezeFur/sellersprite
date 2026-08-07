package cyou.yuanbaomao.sellersprite.research.event;

import lombok.Builder;
import lombok.Value;

/** 写入市场调研统一事件流的命令。 */
@Value
@Builder
public class ResearchEventCommand {

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
}
