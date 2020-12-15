# JOB_HOST=localhost:8881
JOB_HOST=http://35.227.37.213

curl -X POST -H "Authorization: Bearer ${TEST_USER_TOKEN}" -H "Content-Type: application/json" --data '{"name": "manual_freestyle_job_name", "dockerImage": "busybox:1.31", "command": ["sh", "-c", "echo \"Hello world\""]}' $JOB_HOST/jobs
curl -X GET -H "Authorization: Bearer ${TEST_USER_TOKEN}" $JOB_HOST/jobs/1
curl -X POST -H "Authorization: Bearer ${TEST_USER_TOKEN}" -H "Content-Type: application/json" --data '{"name": "ENV"}' $JOB_HOST/jobs/1/parameters
curl -X POST -H "Authorization: Bearer ${TEST_USER_TOKEN}" -H "Content-Type: application/json" --data '{"ENV": "foo"}' $JOB_HOST/jobs/1/runs
curl -X GET -H "Authorization: Bearer ${TEST_USER_TOKEN}" $JOB_HOST/jobs/1/runs/1
curl -X GET -H "Authorization: Bearer ${TEST_USER_TOKEN}" $JOB_HOST/jobs/1/runs/1/console

curl -X DELETE $JOB_HOST/jobs/1

### Invalid job defination:
curl -X POST $JOB_HOST/jobs
# {"timestamp":"2020-06-06T03:01:02.568+0000","status":400,"error":"Bad Request","message":"Required request body is missing: public restfulci.job.shared.domain.JobBean restfulci.job.master.api.JobsController.createJob(restfulci.job.master.dto.JobDTO) throws java.io.IOException","path":"/jobs"}%

curl -X POST -H "Content-Type: application/json" --data '{}' $JOB_HOST/jobs
# {"timestamp":"2020-06-04T04:23:26.748+0000","status":400,"error":"Bad Request","errors":[{"codes":["NotNull.jobDTO.name","NotNull.name","NotNull.java.lang.String","NotNull"],"arguments":[{"codes":["jobDTO.name","name"],"arguments":null,"defaultMessage":"name","code":"name"}],"defaultMessage":"must not be null","objectName":"jobDTO","field":"name","rejectedValue":null,"bindingFailure":false,"code":"NotNull"}],"message":"Validation failed for object='jobDTO'. Error count: 1","path":"/jobs"}%

curl -X POST -H "Content-Type: application/json" --data '{"name": "manual_freestyle_job_name"}' $JOB_HOST/jobs
# {"timestamp":"2020-06-05T03:54:47.547+0000","status":400,"error":"Bad Request","message":"The job definition doesn't fit any existing job types.","path":"/jobs"}%

### Invalid run:

curl -X POST -H "Content-Type: application/json" --data '{"name": "manual_freestyle_job_name", "dockerImage": "busybox:1.31", "command": ["sh", "-c", "echo \"Hello world\""]}' $JOB_HOST/jobs
curl -X GET $JOB_HOST/jobs/1
curl -X POST $JOB_HOST/jobs/1/runs
# {"timestamp":"2020-06-06T03:02:25.732+0000","status":400,"error":"Bad Request","message":"Required request body is missing: public restfulci.job.shared.domain.RunBean restfulci.job.master.api.RunsController.triggerRun(java.lang.Integer,restfulci.job.master.dto.RunDTO) throws java.lang.Exception","path":"/jobs/1/runs"}%

curl -X DELETE $JOB_HOST/jobs/1

### With input:
curl -X POST -H "Content-Type: application/json" --data '{"name": "manual_freestyle_job_name", "dockerImage": "busybox:1.31", "command": ["sh", "-c", "expr $MINUEND - $SUBTRAHEND"]}' $JOB_HOST/jobs
curl -X POST -H "Content-Type: application/json" --data '{"name": "MINUEND"}' $JOB_HOST/jobs/1/parameters
curl -X POST -H "Content-Type: application/json" --data '{"name": "SUBTRAHEND"}' $JOB_HOST/jobs/1/parameters
curl -X POST -H "Content-Type: application/json" --data '{"MINUEND": 5, "SUBTRAHEND": 3}' $JOB_HOST/jobs/1/runs
curl -X POST -H "Content-Type: application/json" --data '{}' $JOB_HOST/jobs/1/runs # with invalid input
# {"timestamp":"2020-06-04T04:37:37.154+0000","status":400,"error":"Bad Request","message":"Missing input for MINUEND","path":"/jobs/1/runs"}%

curl -X DELETE $JOB_HOST/jobs/1

### Job run type mismatch:
curl -X POST -H "Content-Type: application/json" --data '{"name": "manual_freestyle_job_name", "dockerImage": "busybox:1.31", "command": ["sh", "-c", "echo \"Hello world\""]}' $JOB_HOST/jobs
curl -X POST -H "Content-Type: application/json" --data '{"branchName": "master"}' $JOB_HOST/jobs/1/runs
# {"timestamp":"2020-06-05T04:24:09.731+0000","status":400,"error":"Bad Request","message":"Run input doesn't match freestyle job.","path":"/jobs/1/runs"}%

curl -X DELETE $JOB_HOST/jobs/1
