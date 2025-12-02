package com.rizky.keycloak.federationdb;

import org.keycloak.component.ComponentModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.storage.UserStorageProviderFactory;

import java.util.Arrays;
import java.util.List;

public class SybaseFederationProviderFactory implements UserStorageProviderFactory<SybaseFederationProvider> {

    public static final String PROVIDER_ID = "sybase-federation";

    private static final String CFG_JDBC_URL = "jdbcUrl";
    private static final String CFG_DB_USER  = "dbUser";
    private static final String CFG_DB_PASS  = "dbPass";
    private static final String CFG_ENTITLEMENT_REST_SERVICE_BASE_URL = "entitlementRestServiceBaseUrl";

    private static final List<ProviderConfigProperty> CONFIG_PROPERTIES = Arrays.asList(
        prop(CFG_JDBC_URL, "JDBC URL", "jdbc:sybase:Tds://host:5000/db", ProviderConfigProperty.STRING_TYPE),
        prop(CFG_DB_USER, "DB User", "Database username", ProviderConfigProperty.STRING_TYPE),
        prop(CFG_DB_PASS, "DB Password", "Database password", ProviderConfigProperty.PASSWORD),
        prop(CFG_ENTITLEMENT_REST_SERVICE_BASE_URL, "Entitlement REST Service Base URL",
            "Base URL of the Entitlement REST Service", ProviderConfigProperty.STRING_TYPE)
    );

    private static ProviderConfigProperty prop(String name, String label, String help, String type) {
        ProviderConfigProperty p = new ProviderConfigProperty();
        p.setName(name);
        p.setLabel(label);
        p.setHelpText(help);
        p.setType(type);
        return p;
    }

    // --------------------------
    // CREATE PROVIDER
    // --------------------------
    @Override
    public SybaseFederationProvider create(KeycloakSession session, ComponentModel model) {

        String url  = get(model, CFG_JDBC_URL, System.getenv("KC_SYBASE_URL"));
        String user = get(model, CFG_DB_USER,  System.getenv("KC_SYBASE_USER"));
        String pass = get(model, CFG_DB_PASS,  System.getenv("KC_SYBASE_PASS"));
        String entitlementRestServiceBaseUrl = get(model, CFG_ENTITLEMENT_REST_SERVICE_BASE_URL,
            System.getenv("KC_SYBASE_ENTITLEMENT_REST_SERVICE_BASE_URL"));

        if (url == null || user == null || pass == null) {
            throw new IllegalStateException("Missing Sybase config (jdbcUrl/dbUser/dbPass). Set in federation config or env KC_SYBASE_*.");
        }
        return new SybaseFederationProvider(session, model, url, user, pass, entitlementRestServiceBaseUrl);
    }

    private String get(ComponentModel model, String key, String fallback) {
        String v = model.get(key);
        return v != null && !v.isEmpty() ? v : fallback;
    }

    @Override
    public String getId() {
        return PROVIDER_ID;
    }

    @Override
    public List<ProviderConfigProperty> getConfigProperties() {
        return CONFIG_PROPERTIES;
    }
}

