# PIPELINE_HOST=localhost:8882
PIPELINE_HOST=34.74.148.55

curl -X POST -H "Content-Type: application/json" --data '{"name": "manual_pipeline_name"}' $PIPELINE_HOST/pipelines
curl -X GET $PIPELINE_HOST/pipelines/1
curl -X POST -H "Content-Type: application/json" --data '{"originalJobId": 1}' $PIPELINE_HOST/pipelines/1/referred-jobs
curl -X POST -H "Content-Type: application/json" --data '{"ENV": "stage"}' $PIPELINE_HOST/pipelines/1/cycles
curl -X GET $PIPELINE_HOST/pipelines/1/cycles/1

curl -X DELETE $PIPELINE_HOST/pipelines/1

### 500
# {"timestamp":"2020-06-06T03:19:23.568+0000","status":500,"error":"Internal Server Error","message":"could not execute statement; SQL [n/a]; constraint [pipeline_name_key]; nested exception is org.hibernate.exception.ConstraintViolationException: could not execute statement","path":"/pipelines"}%

### 404
# {"timestamp":"2020-06-06T03:20:25.316+0000","status":404,"error":"Not Found","message":"No message available","path":"/jobs/1234"}%
