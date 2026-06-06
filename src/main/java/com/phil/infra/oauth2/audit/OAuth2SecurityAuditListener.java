package com.phil.infra.oauth2.audit;

import org.springframework.context.ApplicationListener;
import org.springframework.security.authentication.event.AbstractAuthenticationEvent;
import org.springframework.security.authentication.event.AbstractAuthenticationFailureEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.authentication.event.LogoutSuccessEvent;
import org.springframework.stereotype.Component;

/**
 * 将 Spring Security 认证事件转为 OAuth2 审计事件。
 */
@Component
public class OAuth2SecurityAuditListener implements ApplicationListener<AbstractAuthenticationEvent> {

    private final OAuth2AuditEventPublisher auditEventPublisher;

    public OAuth2SecurityAuditListener(OAuth2AuditEventPublisher auditEventPublisher) {
        this.auditEventPublisher = auditEventPublisher;
    }

    @Override
    public void onApplicationEvent(AbstractAuthenticationEvent event) {
        if (event instanceof LogoutSuccessEvent) {
            auditEventPublisher.publishSuccess("logout", event.getAuthentication());
            return;
        }
        if (event instanceof AuthenticationSuccessEvent) {
            auditEventPublisher.publishSuccess("authentication", event.getAuthentication());
            return;
        }
        if (event instanceof AbstractAuthenticationFailureEvent failureEvent) {
            auditEventPublisher.publishFailure("authentication", failureEvent.getAuthentication(),
                    failureEvent.getException());
        }
    }
}
