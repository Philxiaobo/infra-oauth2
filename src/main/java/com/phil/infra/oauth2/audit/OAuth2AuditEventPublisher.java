package com.phil.infra.oauth2.audit;

import java.time.Clock;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * OAuth2 安全审计事件发布器。
 */
@Component
public class OAuth2AuditEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(OAuth2AuditEventPublisher.class);

    private final ApplicationEventPublisher eventPublisher;
    private final Clock clock;

    @Autowired
    public OAuth2AuditEventPublisher(ApplicationEventPublisher eventPublisher) {
        this(eventPublisher, Clock.systemUTC());
    }

    OAuth2AuditEventPublisher(ApplicationEventPublisher eventPublisher, Clock clock) {
        this.eventPublisher = eventPublisher;
        this.clock = clock;
    }

    /**
     * 发布成功审计事件。
     *
     * @param eventType 事件类型
     * @param authentication 认证对象
     */
    public void publishSuccess(String eventType, Authentication authentication) {
        publish(eventType, OAuth2AuditEvent.Outcome.SUCCESS, authentication, Map.of());
    }

    /**
     * 发布失败审计事件。
     *
     * @param eventType 事件类型
     * @param authentication 认证对象
     * @param exception 认证异常
     */
    public void publishFailure(String eventType, Authentication authentication, AuthenticationException exception) {
        Map<String, String> details = new LinkedHashMap<>();
        details.put("exception", exception.getClass().getSimpleName());
        if (StringUtils.hasText(exception.getMessage())) {
            details.put("message", exception.getMessage());
        }
        publish(eventType, OAuth2AuditEvent.Outcome.FAILURE, authentication, details);
    }

    private void publish(String eventType,
            OAuth2AuditEvent.Outcome outcome,
            Authentication authentication,
            Map<String, String> details) {
        OAuth2AuditEvent event = new OAuth2AuditEvent(
                eventType,
                outcome,
                principal(authentication),
                Map.copyOf(details),
                Instant.now(clock));
        eventPublisher.publishEvent(event);
        log.info("oauth2 audit eventType={} outcome={} principal={}",
                event.eventType(), event.outcome(), event.principal());
    }

    private String principal(Authentication authentication) {
        if (authentication == null || !StringUtils.hasText(authentication.getName())) {
            return "anonymous";
        }
        return authentication.getName();
    }
}
