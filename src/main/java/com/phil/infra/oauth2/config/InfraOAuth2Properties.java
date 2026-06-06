package com.phil.infra.oauth2.config;

import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * infra-oauth2 配置属性。
 */
@ConfigurationProperties(prefix = "infra.oauth2")
public class InfraOAuth2Properties {

    private String issuer = "http://localhost:9000";

    private Token token = new Token();

    private List<Client> clients = defaultClients();

    private List<User> users = defaultUsers();

    public String getIssuer() {
        return issuer;
    }

    public void setIssuer(String issuer) {
        this.issuer = issuer;
    }

    public Token getToken() {
        return token;
    }

    public void setToken(Token token) {
        this.token = token;
    }

    public List<Client> getClients() {
        return clients;
    }

    public void setClients(List<Client> clients) {
        this.clients = clients;
    }

    public List<User> getUsers() {
        return users;
    }

    public void setUsers(List<User> users) {
        this.users = users;
    }

    private static List<Client> defaultClients() {
        Client browser = new Client();
        browser.setId("browser-client");
        browser.setClientId("browser-client");
        browser.setClientName("Infra OAuth2 Browser Client");
        browser.setClientAuthenticationMethods(setOf("none"));
        browser.setAuthorizationGrantTypes(setOf("authorization_code", "refresh_token"));
        browser.setRedirectUris(setOf("http://127.0.0.1:3000/login/oauth2/code/infra-oauth2"));
        browser.setScopes(setOf("openid", "profile", "push:connect"));
        browser.setRequireProofKey(true);
        browser.setRequireAuthorizationConsent(false);

        Client service = new Client();
        service.setId("service-client");
        service.setClientId("service-client");
        service.setClientSecret("{noop}infra-oauth2-dev-service-secret");
        service.setClientName("Infra Internal Service Client");
        service.setClientAuthenticationMethods(setOf("client_secret_basic"));
        service.setAuthorizationGrantTypes(setOf("client_credentials"));
        service.setScopes(setOf("gateway:authorize", "push:connect", "token:introspect"));

        return List.of(browser, service);
    }

    private static List<User> defaultUsers() {
        User user = new User();
        user.setUsername("infra-admin");
        user.setPassword("{noop}infra-oauth2-dev-password");
        user.setAuthorities(setOf("ROLE_ADMIN", "SCOPE_openid", "SCOPE_profile"));
        return List.of(user);
    }

    private static LinkedHashSet<String> setOf(String... values) {
        return new LinkedHashSet<>(List.of(values));
    }

    /**
     * Token 默认策略。
     */
    public static class Token {

        private Duration authorizationCodeTimeToLive = Duration.ofMinutes(5);

        private Duration accessTokenTimeToLive = Duration.ofMinutes(10);

        private Duration refreshTokenTimeToLive = Duration.ofDays(30);

        private boolean reuseRefreshTokens = false;

        private List<String> audiences = new ArrayList<>(List.of("infra-gateway", "infra-push"));

        public Duration getAuthorizationCodeTimeToLive() {
            return authorizationCodeTimeToLive;
        }

        public void setAuthorizationCodeTimeToLive(Duration authorizationCodeTimeToLive) {
            this.authorizationCodeTimeToLive = authorizationCodeTimeToLive;
        }

        public Duration getAccessTokenTimeToLive() {
            return accessTokenTimeToLive;
        }

        public void setAccessTokenTimeToLive(Duration accessTokenTimeToLive) {
            this.accessTokenTimeToLive = accessTokenTimeToLive;
        }

        public Duration getRefreshTokenTimeToLive() {
            return refreshTokenTimeToLive;
        }

        public void setRefreshTokenTimeToLive(Duration refreshTokenTimeToLive) {
            this.refreshTokenTimeToLive = refreshTokenTimeToLive;
        }

        public boolean isReuseRefreshTokens() {
            return reuseRefreshTokens;
        }

        public void setReuseRefreshTokens(boolean reuseRefreshTokens) {
            this.reuseRefreshTokens = reuseRefreshTokens;
        }

        public List<String> getAudiences() {
            return audiences;
        }

        public void setAudiences(List<String> audiences) {
            this.audiences = audiences;
        }
    }

    /**
     * OAuth2 seed client。
     */
    public static class Client {

        private String id;

        private String clientId;

        private String clientSecret;

        private String clientName;

        private Set<String> clientAuthenticationMethods = new LinkedHashSet<>();

        private Set<String> authorizationGrantTypes = new LinkedHashSet<>();

        private Set<String> redirectUris = new LinkedHashSet<>();

        private Set<String> scopes = new LinkedHashSet<>();

        private boolean requireProofKey;

        private boolean requireAuthorizationConsent;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getClientId() {
            return clientId;
        }

        public void setClientId(String clientId) {
            this.clientId = clientId;
        }

        public String getClientSecret() {
            return clientSecret;
        }

        public void setClientSecret(String clientSecret) {
            this.clientSecret = clientSecret;
        }

        public String getClientName() {
            return clientName;
        }

        public void setClientName(String clientName) {
            this.clientName = clientName;
        }

        public Set<String> getClientAuthenticationMethods() {
            return clientAuthenticationMethods;
        }

        public void setClientAuthenticationMethods(Set<String> clientAuthenticationMethods) {
            this.clientAuthenticationMethods = clientAuthenticationMethods;
        }

        public Set<String> getAuthorizationGrantTypes() {
            return authorizationGrantTypes;
        }

        public void setAuthorizationGrantTypes(Set<String> authorizationGrantTypes) {
            this.authorizationGrantTypes = authorizationGrantTypes;
        }

        public Set<String> getRedirectUris() {
            return redirectUris;
        }

        public void setRedirectUris(Set<String> redirectUris) {
            this.redirectUris = redirectUris;
        }

        public Set<String> getScopes() {
            return scopes;
        }

        public void setScopes(Set<String> scopes) {
            this.scopes = scopes;
        }

        public boolean isRequireProofKey() {
            return requireProofKey;
        }

        public void setRequireProofKey(boolean requireProofKey) {
            this.requireProofKey = requireProofKey;
        }

        public boolean isRequireAuthorizationConsent() {
            return requireAuthorizationConsent;
        }

        public void setRequireAuthorizationConsent(boolean requireAuthorizationConsent) {
            this.requireAuthorizationConsent = requireAuthorizationConsent;
        }
    }

    /**
     * 本地 seed 用户。
     */
    public static class User {

        private String username;

        private String password;

        private Set<String> authorities = new LinkedHashSet<>();

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public Set<String> getAuthorities() {
            return authorities;
        }

        public void setAuthorities(Set<String> authorities) {
            this.authorities = authorities;
        }
    }
}
