docker run \
    -e KEYCLOAK_URL=http://localhost:8880/auth \
    -e KEYCLOAK_USER=restfulci \
    -e KEYCLOAK_PASSWORD=secretpassword \
    -e IMPORT_PATH=/config \
    -e IMPORT_FORCE=false \
    -v /Users/congxin/Workspace/restfulci/auth-microservice/config:/config \
    adorsys/keycloak-config-cli:v2.6.2-11.0.3

# Not working yet. Errors out with `2020-12-02 12:11:12.968 ERROR 1 --- [           main] d.a.k.config.KeycloakConfigRunner        : RESTEASY004655: Unable to invoke request: org.apache.http.conn.HttpHostConnectException: Connect to localhost:8880 [localhost/127.0.0.1] failed: Connection refused (Connection refused)`
