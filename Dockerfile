FROM quay.io/keycloak/keycloak:21.1.2

# Copy both JARs into providers directory
COPY keycloak-federationDB.jar /opt/keycloak/providers/
COPY jconn4.jar /opt/keycloak/providers/

# Run Quarkus augmentation to include both JARs in runtime classpath
RUN /opt/keycloak/bin/kc.sh build

# Optional: set env vars here if you want
ENV KC_SYBASE_URL="jdbc:sybase:Tds:dnjsybdb.drf.corp:5000/common_db"
ENV KC_SYBASE_USER="memberCenter"
# NOTE: Pass the password during docker build or run time.
# ❗ Contact drf-developers to get the correct KC_SYBASE_PASS value.
ENV KC_SYBASE_PASS=""

ENV KC_SYBASE_ENTITLEMENT_REST_SERVICE_BASE_URL="http://dnjhuappesvc01:8080/entitlements-rest-service"
# Increase user storage provider timeout (default is 3000ms)
ENV KC_SPI_USER_STORAGE_PROVIDER_TIMEOUT=10000

# Start Keycloak in dev mode
ENTRYPOINT ["/opt/keycloak/bin/kc.sh", "start-dev"]
