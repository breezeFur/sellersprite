package com.yuanbaomao.sellersprite.common.result;

import com.yuanbaomao.base.result.ErrorCode;
import lombok.Getter;

@Getter
public enum ResultCode implements ErrorCode {

    SUCCESS("00000", "操作成功"),
    PARAM_INVALID("P400", "请求参数不合法"),
    UNAUTHORIZED("A401", "请先登录"),
    SESSION_EXPIRED("A401", "会话已过期，请重新登录"),
    REFRESH_TOKEN_REUSED("A401", "会话凭据已失效，请重新登录"),
    FORBIDDEN("A403", "没有接口访问权限"),
    CURRENT_USER_OPERATION_FORBIDDEN("A403", "不能对当前登录用户执行该操作"),
    RESOURCE_NOT_FOUND("D404", "数据不存在"),
    RESOURCE_CONFLICT("D409", "资源仍被引用，无法执行当前操作"),
    USER_NOT_FOUND("D404", "用户不存在"),
    USERNAME_ALREADY_EXISTS("D409", "用户名已存在"),
    ROLE_NOT_FOUND("D404", "角色不存在"),
    ROLE_CODE_ALREADY_EXISTS("D409", "角色编码已存在"),
    API_PERMISSION_CODE_ALREADY_EXISTS("D409", "接口权限编码已存在"),
    DEPT_NOT_FOUND("D404", "部门不存在"),
    DEPT_CODE_ALREADY_EXISTS("D409", "部门编码已存在"),
    FUNCTION_NOT_FOUND("D404", "功能不存在"),
    FUNCTION_CODE_ALREADY_EXISTS("D409", "功能编码已存在"),
    DICT_TYPE_NOT_FOUND("D404", "字典类型不存在"),
    DICT_TYPE_ALREADY_EXISTS("D409", "字典类型已存在"),
    DICT_DATA_LABEL_ALREADY_EXISTS("D409", "字典标签已存在"),
    AGENT_NOT_FOUND("D404", "未知 Agent"),
    SKILL_NOT_FOUND("D404", "未知 Skill"),
    TOOL_NOT_FOUND("D404", "未知 Tool"),
    APPROVAL_NOT_FOUND("D404", "未知审核任务"),
    APPROVAL_ALREADY_DECIDED("B409", "审核任务已处理"),
    MODEL_RESPONSE_EMPTY("M502", "模型返回为空"),
    AI_MODEL_DISABLED("A503", "AI 模型未启用"),
    AI_MODEL_NOT_CONFIGURED("A503", "AI 模型未配置"),
    AI_CONVERSATION_NOT_FOUND("D404", "AI 会话不存在"),
    AI_MESSAGE_NOT_RETRYABLE("B409", "当前消息不可重试"),
    SELLERSPRITE_DISABLED("S503", "SellerSprite 接口未启用"),
    SELLERSPRITE_NOT_CONFIGURED("S503", "SellerSprite 接口密钥未配置"),
    SELLERSPRITE_PARAM_ERROR("S400", "SellerSprite 请求参数错误"),
    SELLERSPRITE_AUTH_ERROR("S401", "SellerSprite 接口认证失败"),
    SELLERSPRITE_QUOTA_EXHAUSTED("S429", "SellerSprite 接口可用次数已耗尽"),
    SELLERSPRITE_HTTP_ERROR("S502", "SellerSprite 上游 HTTP 请求失败"),
    SELLERSPRITE_PROTOCOL_ERROR("S502", "SellerSprite 上游响应格式错误"),
    SELLERSPRITE_UPSTREAM_ERROR("S502", "SellerSprite 上游业务处理失败"),
    SELLERSPRITE_TIMEOUT("S504", "SellerSprite 上游请求超时"),
    INTERNAL_ERROR("-1", "系统异常，请稍后重试");

    private final String code;
    private final String message;

    ResultCode(String code, String message) {
        this.code = code;
        this.message = message;
    }
}
