# JOB_HOST=localhost:8881
JOB_HOST=http://35.237.33.233

curl -X POST -H "Content-Type: application/json" --data '{"name": "manual_freestyle_job_name", "dockerImage": "busybox:1.31", "command": ["sh", "-c", "echo \"Hello world\""]}' $JOB_HOST/jobs
curl -X GET $JOB_HOST/jobs/1
curl -X POST -H "Content-Type: application/json" --data '{}' $JOB_HOST/jobs/1/runs
curl -X GET $JOB_HOST/jobs/1/runs/1
curl -X GET $JOB_HOST/jobs/1/runs/1/console

curl -X DELETE $JOB_HOST/jobs/1

# Invalid job defination:
curl -X POST -H "Content-Type: application/json" --data '{}' $JOB_HOST/jobs

# With input:
curl -X POST -H "Content-Type: application/json" --data '{"name": "manual_freestyle_job_name", "dockerImage": "busybox:1.31", "command": ["sh", "-c", "expr $MINUEND - $SUBTRAHEND"]}' $JOB_HOST/jobs
curl -X POST -H "Content-Type: application/json" --data '{"name": "MINUEND"}' $JOB_HOST/jobs/1/parameters
curl -X POST -H "Content-Type: application/json" --data '{"name": "SUBTRAHEND"}' $JOB_HOST/jobs/1/parameters
curl -X POST -H "Content-Type: application/json" --data '{"MINUEND": 5, "SUBTRAHEND": 3}' $JOB_HOST/jobs/1/runs
curl -X POST -H "Content-Type: application/json" --data '{}' $JOB_HOST/jobs/1/runs # with invalid input

curl -X DELETE $JOB_HOST/jobs/1
