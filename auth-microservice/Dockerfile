FROM quay.io/keycloak/keycloak:11.0.2

ARG AUTH_ENV

RUN echo "AUTH_ENV: $AUTH_ENV"

COPY "realm-$AUTH_ENV.json" /config/realm.json
ENV KEYCLOAK_IMPORT="/config/realm.json -Dkeycloak.profile.feature.upload_scripts=enabled"
