package cyou.yuanbaomao.sellersprite.ai.research.curation.agent;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class ReActAgent extends BaseAgent {

    public abstract boolean think();

    public abstract String act();

    @Override
    public String step() {
        try {
            if (!think()) {
                return "think finished and no act";
            }
            return act();
        } catch (RuntimeException ex) {
            setState(AgentState.FAILED);
            log.warn("Agent step 执行失败，agentName={}", getName(), ex);
            throw ex;
        }
    }
}
