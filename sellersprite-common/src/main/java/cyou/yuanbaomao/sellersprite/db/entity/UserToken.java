package cyou.yuanbaomao.sellersprite.db.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@TableName("user_token")
@EqualsAndHashCode(callSuper = true)
@Schema(description = "用户令牌实体")
public class UserToken extends BaseAudit {

    @TableId("user_token_id")
    @Schema(description = "用户令牌ID")
    private String userTokenId;

    @TableField("user_id")
    @Schema(description = "用户ID")
    private String userId;

    @TableField("access_token_hash")
    @Schema(description = "访问令牌哈希")
    private String accessTokenHash;

    @TableField("refresh_token_hash")
    @Schema(description = "刷新令牌哈希")
    private String refreshTokenHash;

    @TableField("session_family_id")
    @Schema(description = "刷新会话链ID")
    private String sessionFamilyId;

    @TableField("replaced_by_token_id")
    @Schema(description = "轮换后的令牌ID")
    private String replacedByTokenId;

    @TableField("token_type")
    @Schema(description = "令牌类型")
    private String tokenType;

    @TableField("device_id")
    @Schema(description = "设备ID")
    private String deviceId;

    @TableField("device_name")
    @Schema(description = "设备名称")
    private String deviceName;

    @TableField("client_type")
    @Schema(description = "客户端类型")
    private String clientType;

    @TableField("login_ip")
    @Schema(description = "登录IP")
    private String loginIp;

    @TableField("user_agent")
    @Schema(description = "用户代理")
    private String userAgent;

    @TableField("issued_at")
    @Schema(description = "签发时间，Unix毫秒")
    private Long issuedAt;

    @TableField("expires_at")
    @Schema(description = "过期时间，Unix毫秒")
    private Long expiresAt;

    @TableField("refresh_expires_at")
    @Schema(description = "刷新令牌过期时间，Unix毫秒")
    private Long refreshExpiresAt;

    @TableField("last_used_at")
    @Schema(description = "最近刷新时间，Unix毫秒")
    private Long lastUsedAt;

    @TableField("revoked_at")
    @Schema(description = "撤销时间，Unix毫秒")
    private Long revokedAt;

    @TableField("revoke_reason")
    @Schema(description = "撤销原因")
    private String revokeReason;

    @TableField("status")
    @Schema(description = "状态：1有效 0失效")
    private Integer status;
}
