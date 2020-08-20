JOB_HOST=35.231.89.206
JOB_HOST=localhost:8881

curl -X POST -H "Content-Type: application/json" --data '{"name": "manual_git_job_name", "remoteOrigin": "https://github.com/restfulci/restfulci-examples.git", "configFilepath": "python-pytest/restfulci.yml"}' $JOB_HOST/jobs
curl -X GET $JOB_HOST/jobs/1
curl -X POST -H "Content-Type: application/json" --data '{"branchName": "master"}' $JOB_HOST/jobs/1/runs
curl -X GET $JOB_HOST/jobs/1/runs/1
curl -X GET $JOB_HOST/jobs/1/runs/1/configuration
curl -X GET $JOB_HOST/jobs/1/runs/1/console
curl -X GET $JOB_HOST/jobs/1/runs/1/results/1

curl -X DELETE $JOB_HOST/jobs/1
