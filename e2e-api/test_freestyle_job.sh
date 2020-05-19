RESTFULCI_HOST=localhost:8881

curl -X POST -H "Content-Type: application/json" --data '{"name": "manual_freestyle_job_name", "dockerImage": "busybox:1.31", "command": ["sh", "-c", "echo \"Hello world\""]}' $RESTFULCI_HOST/jobs
curl -X GET $RESTFULCI_HOST/jobs/1
curl -X POST -H "Content-Type: application/json" --data '{}' $RESTFULCI_HOST/jobs/1/runs
curl -X GET $RESTFULCI_HOST/jobs/1/runs/1
curl -X GET $RESTFULCI_HOST/jobs/1/runs/1/console

curl -X DELETE $RESTFULCI_HOST/jobs/1
