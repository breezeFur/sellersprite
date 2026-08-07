package cyou.yuanbaomao.sellersprite.ai.research.curation.agent.advisor;

import java.util.List;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.client.advisor.api.BaseChatMemoryAdvisor;

public final class AmazonSelectionAdvisorSupport {

    private static final String QUESTION_ANSWER_ADVISOR_CLASS_NAME =
            "org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor";

    private AmazonSelectionAdvisorSupport() {
    }

    public static List<Advisor> compactAdvisors(List<Advisor> advisors) {
        if (advisors == null || advisors.isEmpty()) {
            return List.of();
        }
        return advisors.stream()
                .filter(advisor -> !(advisor instanceof BaseChatMemoryAdvisor))
                .filter(advisor -> !hasType(advisor.getClass(), QUESTION_ANSWER_ADVISOR_CLASS_NAME))
                .toList();
    }

    private static boolean hasType(Class<?> type, String expectedClassName) {
        Class<?> current = type;
        while (current != null) {
            if (expectedClassName.equals(current.getName())) {
                return true;
            }
            for (Class<?> interfaceType : current.getInterfaces()) {
                if (hasType(interfaceType, expectedClassName)) {
                    return true;
                }
            }
            current = current.getSuperclass();
        }
        return false;
    }
}
