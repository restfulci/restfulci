# PIPELINE_HOST=localhost:8882
PIPELINE_HOST=34.73.68.176

curl -X POST -H "Content-Type: application/json" --data '{"name": "manual_pipeline_name"}' $PIPELINE_HOST/pipelines
curl -X GET $PIPELINE_HOST/pipelines/1
curl -X POST -H "Content-Type: application/json" --data '{"originalJobId": 1}' $PIPELINE_HOST/pipelines/1/referred-jobs
curl -X POST $PIPELINE_HOST/pipelines/1/cycles
curl -X GET $PIPELINE_HOST/pipelines/1/cycles/1

curl -X DELETE $PIPELINE_HOST/pipelines/1
