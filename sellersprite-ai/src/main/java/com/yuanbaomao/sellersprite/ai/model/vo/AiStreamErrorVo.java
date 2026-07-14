package com.yuanbaomao.sellersprite.ai.model.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AiStreamErrorVo {
    private String code;
    private String message;
    private String trackId;
    private Boolean retryable;
}
