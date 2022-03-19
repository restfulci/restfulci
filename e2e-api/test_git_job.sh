# Execute test_auth.sh to create a user

JOB_HOST=35.231.89.206
JOB_HOST=localhost:8881
JOB_HOST=localhost:8080

curl -i -X GET $JOB_HOST/actuator/health

curl -X POST -H "Authorization: Bearer ${TEST_USER_TOKEN}" -H "Content-Type: application/json" --data '{"name": "manual_git_job_name", "remoteOrigin": "https://github.com/restfulci/restfulci-examples.git", "configFilepath": "python-pytest/restfulci.yml"}' $JOB_HOST/jobs
curl -X GET -H "Authorization: Bearer ${TEST_USER_TOKEN}" $JOB_HOST/jobs/1
curl -X POST -H "Authorization: Bearer ${TEST_USER_TOKEN}" -H "Content-Type: application/json" --data '{"branchName": "master"}' $JOB_HOST/jobs/1/runs
curl -X GET -H "Authorization: Bearer ${TEST_USER_TOKEN}" $JOB_HOST/jobs/1/runs/1
curl -X GET -H "Authorization: Bearer ${TEST_USER_TOKEN}" $JOB_HOST/jobs/1/runs/1/configuration
curl -X GET -H "Authorization: Bearer ${TEST_USER_TOKEN}" $JOB_HOST/jobs/1/runs/1/console
curl -X GET -H "Authorization: Bearer ${TEST_USER_TOKEN}" $JOB_HOST/jobs/1/runs/1/results/1

curl -X DELETE -H "Authorization: Bearer ${TEST_USER_TOKEN}" $JOB_HOST/jobs/1
