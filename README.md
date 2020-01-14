# RestfulCI

A CI/CD framework which expose a RESTful API.

## Rough idea

CI/CD job triggering and job status checks as RESTful API calls. E.g. run a unit-test:

```
curl --request POST -H "Authorization: Bearer abc.def.ghi" \
https://my-ci-server.com/job/unit-test \
-d '{"sha": "0000000000000000000000000000000000000000"}'
```

check a unit test status:

```
curl --request GET -H "Authorization: Bearer abc.def.ghi" \
https://my-ci-server.com.com/job/unit-test/12345
```

It may have multiple clients. The below listed clients should be part of the official framework support.

* UI interface (may be server-side rendered)
* Command line tool
* GitHub PR hooks

Framework configuration (see list below) should be setup by API calls. There can then be command line interface, which can further be wrapped as an infrastructure-as-code infrastructure (maybe through Terraform) which can setup those properties in an idempotent way. Relavent data stay in database (deployed application/files stay unchanged).

* Job specific:
	* Job name.
	* Git PR jobs (apply for jobs which can be triggered by GitHub/GitLab/... PR hooks):
		* Git repo centralized location.
		* Job config file relative path in git repo.
	* Freestyle jobs: everything.
* Overall config:
	* What kind of servers (number of cores) can a job run onto. Or this is hard to be customized.

Job configuration/the logic of a particular job (see list below) should be inside of the to-be-tested repo:

* Job command/script.
* (Dockerized) environment the script can be run onto.
	* Unlike in CircleCI it needs special format Dockerfiles (chose one of the official ones and extends on your need through `.circleci/config.yml` `run` command, or create a specific one with strong CircleCI constrain), it should be able to re-use the Dockerfile used for the production or dev environment of the related project. 
* What kind of servers.

It doesn't matter if the job config (in repo) and infrastructure-as-code are in the same production repo, as they are with separated deployment anyway (same as whether app code and AWS setup are in the same repo or not, so may apply the same rule for both of them). However, job configuration should probably go in the same place as where the production infrastructure-as-code in.

Server management and autoscaling should be part of what this framework can support (probably through Kubernetes).

#### Pipeline where to?

Pipeline should be a client side setup, as it breaks independency between jobs/RESTful endpoints. It can be implemented as [batch operation](https://www.codementor.io/blog/batch-endpoints-6olbjay1hd).

* Pipeline in UI client is obvious/through common sense.
* Command line client shouldn't have shared pipeline. User can have their shellscript with multiple steps.
* There may be an pipeline extension GitHub PR hooks client (for deployment after a master commit).
  * But this makes the GitHub PR hooks client not universally applied, but depend on indivitual job. Better approach?

#### Build artifact where to?

* JFrog artifactory
* Upload to/download from S3/docker hub/ECR/...

### Why not other frameworks?

#### Why not Jenkins?

* **UI/client tights together with job logic:** Jenkins UI is defined at the same place as backend job logic. There's no easy way to have different form of clients for different proposes.

#### Why not CircleCI?

* **Whitelisting a wide of IPs**: To use CircleCI, you need to expose your entire cluseter to a very wide list of CircleCI IPs.
* Hard to define jobs which are not PR triggered in the all-in-one YAML config.
* Hard to define multiple (maybe not quite relevant) jobs all based on the same git repo.
