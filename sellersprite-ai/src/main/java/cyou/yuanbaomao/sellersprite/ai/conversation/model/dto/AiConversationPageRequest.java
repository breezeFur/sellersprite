package cyou.yuanbaomao.sellersprite.ai.conversation.model.dto;

import cyou.yuanbaomao.base.result.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "AI 会话分页查询请求")
public class AiConversationPageRequest extends PageQuery {

    @Size(max = 128, message = "会话标题关键字不能超过128个字符")
    @Schema(description = "会话标题关键字")
    private String title;
}
