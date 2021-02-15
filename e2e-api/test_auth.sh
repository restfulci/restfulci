AUTH_HOST='http://keycloak:8080'

AUTH_HOST='http://localhost:8880'

AUTH_HOST='http://localhost:8080'

AUTH_HOST='http://35.190.189.100'

curl -v -X GET "http://localhost:8880/auth/realms/master/.well-known/openid-configuration"

curl -v -X GET "$AUTH_HOST/auth/realms/master/.well-known/openid-configuration"

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
--data-urlencode 'client_id=job-frontend' \
--data-urlencode 'username=test-user' \
--data-urlencode 'password=password' \
--data-urlencode 'grant_type=password' | jq -r '.access_token')

# TEST_USER_TOKEN=$(curl -X POST \
# "$AUTH_HOST/auth/realms/restfulci/protocol/openid-connect/token" \
# --header 'Content-Type: application/x-www-form-urlencoded' \
# --data-urlencode 'client_id=job-microservice' \
# --data-urlencode 'client_secret=dc80857e-b4b7-45ec-ab56-1242ba7600ff' \
# --data-urlencode 'username=test-user' \
# --data-urlencode 'password=password' \
# --data-urlencode 'grant_type=password' | jq -r '.access_token')

echo $TEST_USER_TOKEN

curl -v -H "Authorization: Bearer ${TEST_USER_TOKEN}" "$AUTH_HOST/auth/realms/restfulci/protocol/openid-connect/userinfo"

JOB_HOST=http://localhost:8080
JOB_HOST=http://localhost:8881
curl -v -X OPTIONS $JOB_HOST/jobs
curl -v -H "Authorization: Bearer ${TEST_USER_TOKEN}" $JOB_HOST/jobs
curl -v -H "Authorization: Bearer ${TEST_USER_TOKEN}" $JOB_HOST/pipelines
curl -X POST -H "Content-Type: application/json" -H "Authorization: Bearer ${TEST_USER_TOKEN}" --data '{"name": "manual_freestyle_job_name", "dockerImage": "busybox:1.33", "command": ["sh", "-c", "echo \"Hello world\""]}' $JOB_HOST/jobs
curl -X POST -H "Content-Type: application/json" -H "Authorization: Bearer ${TEST_USER_TOKEN}" --data '{}' $JOB_HOST/jobs/1/runs
curl -v -X DELETE -H "Authorization: Bearer ${TEST_USER_TOKEN}" $JOB_HOST/jobs/2

curl -v -H "Authorization: Bearer ${TEST_USER_TOKEN}" http://localhost:8080/users/me

curl -v -H "Authorization: Bearer ${TEST_USER_TOKEN}" http://localhost:8881/users/me
