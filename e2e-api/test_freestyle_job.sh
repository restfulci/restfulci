# JOB_HOST=localhost:8881
JOB_HOST=http://35.237.33.233

curl -X POST -H "Content-Type: application/json" --data '{"name": "manual_freestyle_job_name", "dockerImage": "busybox:1.31", "command": ["sh", "-c", "echo \"Hello world\""]}' $JOB_HOST/jobs
curl -X GET $JOB_HOST/jobs/1
curl -X POST -H "Content-Type: application/json" --data '{}' $JOB_HOST/jobs/1/runs
curl -X GET $JOB_HOST/jobs/1/runs/1
curl -X GET $JOB_HOST/jobs/1/runs/1/console

curl -X DELETE $JOB_HOST/jobs/1

### Invalid job defination:
curl -X POST -H "Content-Type: application/json" --data '{}' $JOB_HOST/jobs
# {"timestamp":"2020-06-04T04:23:26.748+0000","status":400,"error":"Bad Request","errors":[{"codes":["NotNull.jobDTO.name","NotNull.name","NotNull.java.lang.String","NotNull"],"arguments":[{"codes":["jobDTO.name","name"],"arguments":null,"defaultMessage":"name","code":"name"}],"defaultMessage":"must not be null","objectName":"jobDTO","field":"name","rejectedValue":null,"bindingFailure":false,"code":"NotNull"}],"message":"Validation failed for object='jobDTO'. Error count: 1","path":"/jobs"}%

curl -X POST -H "Content-Type: application/json" --data '{"name": "manual_freestyle_job_name"}' $JOB_HOST/jobs
# {"timestamp":"2020-06-05T03:54:47.547+0000","status":400,"error":"Bad Request","message":"The job definition doesn't fit any existing job types.","path":"/jobs"}%

### With input:
curl -X POST -H "Content-Type: application/json" --data '{"name": "manual_freestyle_job_name", "dockerImage": "busybox:1.31", "command": ["sh", "-c", "expr $MINUEND - $SUBTRAHEND"]}' $JOB_HOST/jobs
curl -X POST -H "Content-Type: application/json" --data '{"name": "MINUEND"}' $JOB_HOST/jobs/1/parameters
curl -X POST -H "Content-Type: application/json" --data '{"name": "SUBTRAHEND"}' $JOB_HOST/jobs/1/parameters
curl -X POST -H "Content-Type: application/json" --data '{"MINUEND": 5, "SUBTRAHEND": 3}' $JOB_HOST/jobs/1/runs
curl -X POST -H "Content-Type: application/json" --data '{}' $JOB_HOST/jobs/1/runs # with invalid input
#{"timestamp":"2020-06-04T04:37:37.154+0000","status":400,"error":"Bad Request","message":"Missing input for MINUEND","path":"/jobs/1/runs"}%

curl -X DELETE $JOB_HOST/jobs/1

### Job run type mismatch:
curl -X POST -H "Content-Type: application/json" --data '{"name": "manual_freestyle_job_name", "dockerImage": "busybox:1.31", "command": ["sh", "-c", "echo \"Hello world\""]}' $JOB_HOST/jobs
curl -X POST -H "Content-Type: application/json" --data '{"branchName": "master"}' $JOB_HOST/jobs/1/runs
# {"timestamp":"2020-06-05T04:24:09.731+0000","status":400,"error":"Bad Request","message":"Run input doesn't match freestyle job.","path":"/jobs/1/runs"}%

curl -X DELETE $JOB_HOST/jobs/1
