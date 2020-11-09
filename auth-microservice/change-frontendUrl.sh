/opt/jboss/keycloak/bin/jboss-cli.sh --connect --command='/subsystem=keycloak-server/spi=hostname/provider=default:write-attribute(name=properties.frontendUrl,value="http://localhost:8880/auth")'
