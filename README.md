# 🚀 Sybase User Federation Provider for Keycloak

Custom Keycloak User Federation implementation backed by a Sybase database
Supports Keycloak v21.1.2 and Java 8.

## ✨ Overview
This project provides a custom User Storage Provider that allows Keycloak to authenticate and fetch users directly from an existing Sybase database.

## 🛠️ Build Instructions
- Clone the repository:
   ```bash
   git clone git@github.com:suraj-haptiq/keycloak-federationDB.git
   cd keycloak-federationDB
    ```
- Build the provider JAR:
    ```bash
    mvn clean package
   ```

- This generates: `target/keycloak-federationDB.jar.jar`

- Set Sybase Credentials:
  - Contact drf-developers to obtain the Sybase development instance password.
  - Update the Dockerfile: `ENV KC_SYBASE_PASS="KC_SYBASE_PASS"`

- 🐳 Docker Instructions
  - Build the Docker image with: `sudo docker build -t keycloak-sybase .`
  - Run the Keycloak container with: `sudo docker run -d -p 8080:8080 -e KEYCLOAK_ADMIN=admin -e KEYCLOAK_ADMIN_PASSWORD=admin keycloak-sybase`

When you run the built Docker image:
1. Start Keycloak in detached mode (-d)
2. Expose port 8080 so you can access Keycloak at http://localhost:8080/
3. Create the Keycloak admin user with the provided username and password
