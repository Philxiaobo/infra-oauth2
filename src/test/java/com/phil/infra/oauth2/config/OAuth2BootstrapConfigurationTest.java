package com.phil.infra.oauth2.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;

import com.phil.infra.oauth2.audit.OAuth2AuditEvent;
import com.phil.infra.oauth2.audit.OAuth2AuditEventPublisher;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;
import org.springframework.test.context.event.ApplicationEvents;
import org.springframework.test.context.event.RecordApplicationEvents;

/**
 * OAuth2 MVP 基线配置测试。
 */
@SpringBootTest
@RecordApplicationEvents
class OAuth2BootstrapConfigurationTest {

    @Autowired
    private RegisteredClientRepository registeredClientRepository;

    @Autowired
    private AuthorizationServerSettings authorizationServerSettings;

    @Autowired
    private OAuth2AuditEventPublisher auditEventPublisher;

    @Autowired
    private ApplicationEvents applicationEvents;

    @Test
    void browserClientUsesAuthorizationCodePkceAndRefreshToken() {
        RegisteredClient client = registeredClientRepository.findByClientId("browser-client");

        assertThat(client).isNotNull();
        assertThat(client.getClientAuthenticationMethods()).containsExactly(ClientAuthenticationMethod.NONE);
        assertThat(client.getAuthorizationGrantTypes())
                .containsExactlyInAnyOrder(AuthorizationGrantType.AUTHORIZATION_CODE,
                        AuthorizationGrantType.REFRESH_TOKEN)
                .doesNotContain(new AuthorizationGrantType("password"), new AuthorizationGrantType("implicit"));
        assertThat(client.getRedirectUris())
                .containsExactly("http://127.0.0.1:3000/login/oauth2/code/infra-oauth2");
        assertThat(client.getScopes()).contains("openid", "profile", "push:connect");
        assertThat(client.getClientSettings().isRequireProofKey()).isTrue();
        assertThat(client.getTokenSettings().getAccessTokenTimeToLive()).isEqualTo(Duration.ofMinutes(10));
        assertThat(client.getTokenSettings().getRefreshTokenTimeToLive()).isEqualTo(Duration.ofDays(30));
        assertThat(client.getTokenSettings().isReuseRefreshTokens()).isFalse();
    }

    @Test
    void serviceClientUsesClientCredentialsOnly() {
        RegisteredClient client = registeredClientRepository.findByClientId("service-client");

        assertThat(client).isNotNull();
        assertThat(client.getClientAuthenticationMethods()).containsExactly(ClientAuthenticationMethod.CLIENT_SECRET_BASIC);
        assertThat(client.getAuthorizationGrantTypes())
                .containsExactly(AuthorizationGrantType.CLIENT_CREDENTIALS)
                .doesNotContain(AuthorizationGrantType.AUTHORIZATION_CODE,
                        AuthorizationGrantType.REFRESH_TOKEN,
                        new AuthorizationGrantType("password"),
                        new AuthorizationGrantType("implicit"));
        assertThat(client.getRedirectUris()).isEmpty();
        assertThat(client.getScopes()).contains("gateway:authorize", "push:connect", "token:introspect");
        assertThat(client.getClientSecret()).startsWith("{noop}");
    }

    @Test
    void authorizationServerSettingsExposeIssuerAndStandardEndpoints() {
        assertThat(authorizationServerSettings.getIssuer()).isEqualTo("http://localhost:9000");
        assertThat(authorizationServerSettings.getJwkSetEndpoint()).isEqualTo("/oauth2/jwks");
        assertThat(authorizationServerSettings.getTokenEndpoint()).isEqualTo("/oauth2/token");
        assertThat(authorizationServerSettings.getTokenRevocationEndpoint()).isEqualTo("/oauth2/revoke");
    }

    @Test
    void auditPublisherPublishesStructuredSuccessEvent() {
        TestingAuthenticationToken authentication =
                new TestingAuthenticationToken("demo-user", "n/a", "ROLE_USER");

        auditEventPublisher.publishSuccess("login", authentication);

        assertThat(applicationEvents.stream(OAuth2AuditEvent.class))
                .anySatisfy(event -> {
                    assertThat(event.eventType()).isEqualTo("login");
                    assertThat(event.outcome()).isEqualTo(OAuth2AuditEvent.Outcome.SUCCESS);
                    assertThat(event.principal()).isEqualTo("demo-user");
                    assertThat(event.occurredAt()).isNotNull();
                });
    }
}
