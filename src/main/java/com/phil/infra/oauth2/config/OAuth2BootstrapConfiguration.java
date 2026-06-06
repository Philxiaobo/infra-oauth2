package com.phil.infra.oauth2.config;

import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.server.authorization.InMemoryOAuth2AuthorizationConsentService;
import org.springframework.security.oauth2.server.authorization.InMemoryOAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationConsentService;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.client.InMemoryRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * OAuth2 MVP seed 数据与基础服务配置。
 */
@Configuration(proxyBeanMethods = false)
public class OAuth2BootstrapConfiguration {

    private static final Set<String> DISABLED_GRANT_TYPES = Set.of("password", "implicit");

    @Bean
    RegisteredClientRepository registeredClientRepository(InfraOAuth2Properties properties) {
        List<RegisteredClient> clients = properties.getClients().stream()
                .map(client -> toRegisteredClient(client, properties.getToken()))
                .toList();
        Assert.notEmpty(clients, "At least one OAuth2 client must be configured");
        return new InMemoryRegisteredClientRepository(clients);
    }

    @Bean
    OAuth2AuthorizationService authorizationService() {
        return new InMemoryOAuth2AuthorizationService();
    }

    @Bean
    OAuth2AuthorizationConsentService authorizationConsentService() {
        return new InMemoryOAuth2AuthorizationConsentService();
    }

    @Bean
    UserDetailsService userDetailsService(InfraOAuth2Properties properties) {
        List<UserDetails> users = properties.getUsers().stream()
                .map(user -> User.withUsername(user.getUsername())
                        .password(user.getPassword())
                        .authorities(user.getAuthorities().toArray(String[]::new))
                        .build())
                .toList();
        Assert.notEmpty(users, "At least one OAuth2 user must be configured");
        return new InMemoryUserDetailsManager(users);
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    private RegisteredClient toRegisteredClient(InfraOAuth2Properties.Client client,
            InfraOAuth2Properties.Token token) {
        RegisteredClient.Builder builder = RegisteredClient.withId(required(client.getId(), "client id"))
                .clientId(required(client.getClientId(), "clientId"))
                .clientName(required(client.getClientName(), "clientName"))
                .clientSettings(ClientSettings.builder()
                        .requireProofKey(client.isRequireProofKey())
                        .requireAuthorizationConsent(client.isRequireAuthorizationConsent())
                        .build())
                .tokenSettings(TokenSettings.builder()
                        .authorizationCodeTimeToLive(token.getAuthorizationCodeTimeToLive())
                        .accessTokenTimeToLive(token.getAccessTokenTimeToLive())
                        .refreshTokenTimeToLive(token.getRefreshTokenTimeToLive())
                        .reuseRefreshTokens(token.isReuseRefreshTokens())
                        .build());

        if (StringUtils.hasText(client.getClientSecret())) {
            builder.clientSecret(client.getClientSecret());
        }
        client.getClientAuthenticationMethods()
                .forEach(method -> builder.clientAuthenticationMethod(toClientAuthenticationMethod(method)));
        client.getAuthorizationGrantTypes().forEach(grant -> builder.authorizationGrantType(toGrantType(grant)));
        client.getRedirectUris().forEach(builder::redirectUri);
        client.getScopes().forEach(builder::scope);
        return builder.build();
    }

    private ClientAuthenticationMethod toClientAuthenticationMethod(String method) {
        String normalized = normalize(method);
        return switch (normalized) {
            case "none" -> ClientAuthenticationMethod.NONE;
            case "client_secret_basic" -> ClientAuthenticationMethod.CLIENT_SECRET_BASIC;
            case "client_secret_post" -> ClientAuthenticationMethod.CLIENT_SECRET_POST;
            case "client_secret_jwt" -> ClientAuthenticationMethod.CLIENT_SECRET_JWT;
            case "private_key_jwt" -> ClientAuthenticationMethod.PRIVATE_KEY_JWT;
            default -> new ClientAuthenticationMethod(normalized);
        };
    }

    private AuthorizationGrantType toGrantType(String grantType) {
        String normalized = normalize(grantType);
        if (DISABLED_GRANT_TYPES.contains(normalized)) {
            throw new IllegalArgumentException("OAuth2 grant type is disabled: " + normalized);
        }
        return switch (normalized) {
            case "authorization_code" -> AuthorizationGrantType.AUTHORIZATION_CODE;
            case "refresh_token" -> AuthorizationGrantType.REFRESH_TOKEN;
            case "client_credentials" -> AuthorizationGrantType.CLIENT_CREDENTIALS;
            case "urn:ietf:params:oauth:grant-type:jwt-bearer" -> AuthorizationGrantType.JWT_BEARER;
            default -> new AuthorizationGrantType(normalized);
        };
    }

    private String normalize(String value) {
        return required(value, "value").toLowerCase(Locale.ROOT);
    }

    private String required(String value, String fieldName) {
        Assert.hasText(value, "OAuth2 " + fieldName + " must not be blank");
        return value;
    }
}
