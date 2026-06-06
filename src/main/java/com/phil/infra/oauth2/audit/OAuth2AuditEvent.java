package com.phil.infra.oauth2.audit;

import java.time.Instant;
import java.util.Map;

/**
 * OAuth2 安全审计事件。
 *
 * @param eventType 事件类型
 * @param outcome 事件结果
 * @param principal 认证主体
 * @param details 附加审计字段
 * @param occurredAt 发生时间
 */
public record OAuth2AuditEvent(
        String eventType,
        Outcome outcome,
        String principal,
        Map<String, String> details,
        Instant occurredAt) {

    /**
     * 审计结果。
     */
    public enum Outcome {
        SUCCESS,
        FAILURE
    }
}
