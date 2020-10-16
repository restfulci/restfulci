AUTH_HOST='http://localhost:8880'

MASTER_TOKEN=$(curl -X POST \
"$AUTH_HOST/auth/realms/master/protocol/openid-connect/token" \
--header 'Content-Type: application/x-www-form-urlencoded' \
--data-urlencode 'client_id=admin-cli' \
--data-urlencode 'username=restfulci' \
--data-urlencode 'password=secretpassword' \
--data-urlencode 'grant_type=password' | jq -r '.access_token')

# Create user
curl -X POST \
"$AUTH_HOST/auth/admin/realms/restfulci/users" \
--header 'Content-Type: application/json' \
--header "Authorization: Bearer $MASTER_TOKEN" \
--data-raw '{"enabled":"true", "username":"test-user"}'

TEST_USER_ID=$(curl -X GET \
"$AUTH_HOST/auth/admin/realms/restfulci/users" \
--header 'Content-Type: application/json' \
--header "Authorization: Bearer $MASTER_TOKEN" \
--data-raw '{"username": "test-user"}' | jq -r '.[0].id')

# Change user password
curl -X PUT \
"$AUTH_HOST/auth/admin/realms/restfulci/users/$TEST_USER_ID/reset-password" \
--header 'Content-Type: application/json' \
--header "Authorization: Bearer $MASTER_TOKEN" \
--data-raw '{ "type":"password", "temporary":false, "value":"password"}'

TEST_USER_TOKEN=$(curl -X POST \
"$AUTH_HOST/auth/realms/restfulci/protocol/openid-connect/token" \
--header 'Content-Type: application/x-www-form-urlencoded' \
--data-urlencode 'client_id=job-microservice' \
--data-urlencode 'client_secret=804ccb10-850e-40d9-aae5-9e69b91af511' \
--data-urlencode 'username=test-user' \
--data-urlencode 'password=password' \
--data-urlencode 'grant_type=password' | jq -r '.access_token')

curl -H "Authorization: Bearer ${TEST_USER_TOKEN}" http://localhost:8080/jobs

curl -H "Authorization: Bearer ${TEST_USER_TOKEN}" http://localhost:8080/users/me
