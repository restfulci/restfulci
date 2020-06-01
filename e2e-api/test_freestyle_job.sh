# JOB_HOST=localhost:8881
JOB_HOST=http://35.237.33.233

curl -X POST -H "Content-Type: application/json" --data '{"name": "manual_freestyle_job_name", "dockerImage": "busybox:1.31", "command": ["sh", "-c", "echo \"Hello world\""]}' $JOB_HOST/jobs
curl -X GET $JOB_HOST/jobs/1
curl -X POST -H "Content-Type: application/json" --data '{}' $JOB_HOST/jobs/1/runs
curl -X GET $JOB_HOST/jobs/1/runs/1
curl -X GET $JOB_HOST/jobs/1/runs/1/console

curl -X DELETE $JOB_HOST/jobs/1
