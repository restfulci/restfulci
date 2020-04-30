RESTFULCI_HOST=35.229.105.19

curl -X POST -H "Content-Type: application/json" --data '{"name": "manual_git_job_name", "remoteOrigin": "https://github.com/restfulci/restfulci-examples.git", "configFilepath": "hello-world/restfulci.yml"}' $RESTFULCI_HOST/jobs
curl -X GET $RESTFULCI_HOST/jobs/1
curl -X POST -H "Content-Type: application/json" --data '{"branchName": "master"}' $RESTFULCI_HOST/jobs/1/runs
curl -X GET $RESTFULCI_HOST/jobs/1/runs/1
curl -X GET $RESTFULCI_HOST/jobs/1/runs/1/configuration
curl -X GET $RESTFULCI_HOST/jobs/1/runs/1/console
curl -X GET $RESTFULCI_HOST/jobs/1/runs/1/results/1

curl -X DELETE $RESTFULCI_HOST/jobs/1
