https://github.com/adorsys/keycloak-config-cli/tree/master/src/test/resources/import-files is a good resource on how to write `realm.json`.

But when there's a syntax error on `realm.json`, it will errors out as

```
keycloak_1           | 12:24:52,202 ERROR [org.jboss.as.controller.management-operation] (Controller Boot Thread) WFLYCTL0013: Operation ("add") failed - address: ([("subsystem" => "microprofile-metrics-smallrye")]): java.lang.NullPointerException
...
keycloak_1           | auth-microservice_keycloak_1 exited with code 1
```

and it is hard to debug why.
