package com.rizky.keycloak.federationdb;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import org.keycloak.component.ComponentModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.storage.UserStorageProvider;
import org.keycloak.storage.user.UserLookupProvider;
import org.keycloak.credential.CredentialInput;
import org.keycloak.credential.CredentialInputValidator;
import org.keycloak.credential.CredentialModel;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

public class SybaseFederationProvider implements UserStorageProvider,
    UserLookupProvider,
    CredentialInputValidator {

    private static final Logger log = LoggerFactory.getLogger(SybaseFederationProvider.class);

    private static final String QUERY_USER =
        "SELECT login_name, first_name FROM prod.prod_customers WHERE login_name = ?";

    private final KeycloakSession session;
    private final ComponentModel model;
    private final String jdbcUrl;
    private final String dbUser;
    private final String dbPass;
    private final String entitlementRestServiceBaseUrl;

    public SybaseFederationProvider(KeycloakSession session, ComponentModel model, String jdbcUrl,
        String dbUser, String dbPass, String entitlementRestServiceBaseUrl) {
        log.info("SybaseFederationProvider initialized with jdbcUrl: {}", jdbcUrl);
        this.session = session;
        this.model = model;
        this.jdbcUrl = jdbcUrl;
        this.dbUser = dbUser;
        this.dbPass = dbPass;
        this.entitlementRestServiceBaseUrl = entitlementRestServiceBaseUrl;
    }

    @Override
    public void close() {
        log.info("SybaseFederationProvider closing...");
    }

    // Keycloak 21+ signatures:
    @Override
    public UserModel getUserByUsername(RealmModel realm, String username) {
        log.info("getUserByUsername() called: {}", username);
        return lookupAndMapUser(realm, QUERY_USER, username.toUpperCase());
    }

    @Override
    public UserModel getUserById(RealmModel realm, String id) {
        log.info("getUserById() called: {}", id);
        return lookupAndMapUser(realm, QUERY_USER, id);
    }

    @Override
    public UserModel getUserByEmail(RealmModel realm, String email) {
        log.info("getUserByEmail() called: {}", email);
        return lookupAndMapUser(realm, QUERY_USER, email.toUpperCase());
    }

    // ----------------------------------------------------------------------
    // MAIN LOOKUP METHOD
    // ----------------------------------------------------------------------
    private UserModel lookupAndMapUser(RealmModel realm, String sql, String value) {
        final String param = value == null ? "" : value.trim();
        log.info("lookupAndMapUser started | SQL: {} | param: '{}'", sql, param);

        try (Connection conn = newConnection();
            PreparedStatement ps = conn.prepareStatement(sql)) {

            conn.setReadOnly(true);
            ps.setQueryTimeout(2);
            ps.setFetchSize(1);
            ps.setString(1, param);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    log.warn("lookupAndMapUser: No record found for: '{}'", param);
                    return null;
                }

                String login = rs.getString("login_name");
                String first = rs.getString("first_name");
                log.info("DB Lookup Success: login_name: {} | first_name: {}", login, first);

                log.info("Creating or fetching local Keycloak user for: '{}'", login);

                // Create locally, linked to this federation provider
                UserModel user;
                try {
                    user = session.users().addUser(realm, login);
                    user.setFederationLink(model.getId());
                    user.setUsername(login);
                    user.setEmail(login); // change if you have a separate email column
                    if (first != null) {
                        user.setFirstName(first);
                    }
                    user.setEnabled(true);
                    user.setEmailVerified(true);
                    log.info("Local federated user created for: '{}'", login);
                } catch (RuntimeException alreadyExists) {
                    log.warn("User already exists locally: {}", login);
                    // If user already in local, fetch it locally (non-federated)
                    user = session.userLocalStorage().getUserByUsername(realm, login);
                    if (user == null) {
                        // As a fallback, fetch by email locally
                        user = session.userLocalStorage().getUserByEmail(realm, login);
                    }
                }

                return user;
            }
        } catch (SQLException e) {
            log.error("Sybase lookup failed: {}", e.getMessage(), e);
            throw new RuntimeException("Sybase lookup failed", e);
        }
    }


    // ----------------------------------------------------------------------
    // PASSWORD VALIDATION
    // ----------------------------------------------------------------------
    @Override
    public boolean isValid(RealmModel realm, UserModel user, CredentialInput input) {
        if (!supportsCredentialType(input.getType())) {
            log.warn("Unsupported credential type: {}", input.getType());
            return false;
        }

        String username = user.getUsername().toUpperCase();
        String providedPassword = input.getChallengeResponse();

        log.info("Password validation started for user: {}", username);

        try {
            username = URLEncoder.encode(username, "UTF-8");
            providedPassword = URLEncoder.encode(providedPassword, "UTF-8");

            String url =
                entitlementRestServiceBaseUrl + "/customer/validateLogin/1?loginName=" + username
                    + "&password=" + providedPassword;

            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<SimpleResponseType> validationEntity = restTemplate.getForEntity(url,
                SimpleResponseType.class);
            SimpleResponseType simpleResponseType = validationEntity.getBody();

            if (simpleResponseType != null) {
                int responseCode = simpleResponseType.getResponseCode();
                String responseDescription = simpleResponseType.getResponseDescription();
                if ((responseCode) == 0 && responseDescription.equalsIgnoreCase("OK")) {
                    return true;
                }
            }
        } catch (UnsupportedEncodingException ex) {
            log.error("Encoding error for username '{}': {}", username, ex.getMessage(), ex);
        }
        return false;
    }

    @Override
    public boolean supportsCredentialType(String credentialType) {
        boolean supports = CredentialModel.PASSWORD.equals(credentialType);
        log.info("supportsCredentialType('{}') : {}", credentialType, supports);
        return supports;
    }

    @Override
    public boolean isConfiguredFor(RealmModel realm, UserModel user, String credentialType) {
        log.debug("isConfiguredFor({}, {}) called", user.getUsername(), credentialType);
        return supportsCredentialType(credentialType);
    }

    // ----------------------------------------------------------------------
    // HELPER: CREATE CONNECTION
    // ----------------------------------------------------------------------
    private Connection newConnection() throws SQLException {
        log.debug("Opening Sybase connection: url: {}, user: {}", jdbcUrl, dbUser);
        return DriverManager.getConnection(jdbcUrl, dbUser, dbPass);
    }

}

