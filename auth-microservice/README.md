https://github.com/adorsys/keycloak-config-cli/tree/master/src/test/resources/import-files is a good resource on how to write `realm.json`.

But when there's a syntax error on `realm.json`, it will errors out as

```
keycloak_1           | 12:24:52,202 ERROR [org.jboss.as.controller.management-operation] (Controller Boot Thread) WFLYCTL0013: Operation ("add") failed - address: ([("subsystem" => "microprofile-metrics-smallrye")]): java.lang.NullPointerException
...
keycloak_1           | auth-microservice_keycloak_1 exited with code 1
```

and it is hard to debug why.

TODO:

Looks like the scope is not right. It should be something like `read write` as

> scope—The actions that the token is permitted to perform on the resource server (microservice).

```
$ curl -X POST \
"$AUTH_HOST/auth/realms/restfulci/protocol/openid-connect/token" \
--header 'Content-Type: application/x-www-form-urlencoded' \
--data-urlencode 'client_id=job-frontend' \
--data-urlencode 'username=test-user' \
--data-urlencode 'password=password' \
--data-urlencode 'grant_type=password' | jq .
{
  "access_token": "****",
  "expires_in": 300,
  "refresh_expires_in": 1800,
  "refresh_token": "****",
  "token_type": "bearer",
  "not-before-policy": 0,
  "session_state": "5e48c597-52fa-41cd-9fa4-0658ca627e62",
  "scope": "profile email"
}
```
