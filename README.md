# RestfulCI

A CI/CD framework which expose a RESTful API.

## Rough idea

CI/CD job triggering and job status checks as RESTful API calls. E.g. run a unit-test:

```
curl --request POST -H "Authorization: Bearer abc.def.ghi" \
https://my-ci-server.com/job/unit-test \
-d '{"sha": "0000000000000000000000000000000000000000"}'
curl --request POST -H "Authorization: Bearer abc.def.ghi" \
https://my-ci-server.com/jobs/unit-test \
-d '{"branch": "master"}'
```

check a unit test status:

```
curl --request GET -H "Authorization: Bearer abc.def.ghi" \
https://my-ci-server.com.com/jobs/unit-test/runs/12345
```

It may have multiple clients. The below listed clients should be part of the official framework support.

* UI interface (may be server-side rendered)
* Command line tool
* GitHub PR hooks

Framework configuration (see list below) should be setup by API calls. There can then be command line interface, which can further be wrapped as an infrastructure-as-code infrastructure (maybe through Terraform) which can setup those properties in an idempotent way. Relavent data stay in database (deployed application/files stay unchanged).

* Job specific:
	* Job name.
	* Job type specific attributes:
		* Git repo jobs (the trigger from GitHub/GitLab/... PR hooks is a different thing/not define in here, and this job can be triggered manually or from a PR hook):
			* Git repo centralized location.
			* Job config file relative path in git repo.
		* Freestyle jobs:
			* Everything.
* Overall config:
	* What kind of servers (number of cores) can a job run onto. Or this is hard to be customized.

Job configuration/the logic of a particular job (see list below) should be inside of the to-be-tested repo:

* Job command/script.
* (Dockerized) environment the script can be run onto.
	* Unlike in CircleCI it needs special format Dockerfiles (chose one of the official ones and extends on your need through `.circleci/config.yml` `run` command, or create a specific one with strong CircleCI constrain), it should be able to re-use the Dockerfile used for the production or dev environment of the related project.
* External dependencies (database, queueing system, ...):
	* Should be defined as sidecar containers.
	 	* Cannot export port and let main script call localhost:port, as that will cause port crashing when multiple jobs are running in the same hosting machine.
		* Will need to setup network in between containers.
		* Feature becomes very similar to docker-compose. Wonder if we can use it as a dependent lib.
	* Alternatively, we can always `RUN apt-get install postgresql` in the main container and access localhost. But that will make user unable to use their prod container for test.
* A list of environmental variables (used to pass in secrets/credentials).
	* Will need the actual credentials saved in backend associate with the job itself. The run will error out if it cannot find some values of the secrets/credentials.
		* First step save plat text in database.
		* Finally we should move them to a secured backend persistent storage, e.g. [HashiCorp Vault](https://www.vaultproject.io/).
	* Follows [The Twelve Factors](https://12factor.net/config) that they should be passed as environmental variables one at a time.
	* Actual pass in the secrets/credentials by `docker --env ENV=foo`. Very unforturate cannot do `docker -e ENV` and define ENV in host, as that will cause ENV name crashing for secrets/credentials belong to different jobs.
		* An alternative approach is to use [Docker swarm secrets](https://docs.docker.com/engine/swarm/secrets/). Needs to investigate more on the possibilities/pros/cons this approach.
		* If user need to do git operations inside of the git container, we need to use SSH agent forwarding for credentials (volume mount `.ssh/id_rsa` inside/outside of the container is not secure, and doesn't work if user uses a passphrase). Refer [here](http://blog.oddbit.com/post/2019-02-24-docker-build-learns-about-secr/), [here](https://github.com/uber-common/docker-ssh-agent-forward), [here](https://medium.com/@nazrulworld/ssh-agent-forward-into-docker-container-on-macos-ff847ec660e2) for detail. We should probably still need to create `.ssh/id_rsa` at some point, safe it to some secret persistent storage (because otherwise it cannot be used by multiple slaves), give user `.ssh/id_rsa.pub` and let them use it to setup 3rd party git servers.
* Timeout
  * There should probably also a global (umbrella) one shared by all jobs.
* What kind of results it should save, and where are they inside of the container after finishing the job.
	* If the plan is to "docker volume link" the result out, and let the slave machine (where slave agent stays) upload the result to persistent storage, we'll have problem cleanup those result files (as they are created/owned by the user inside of the docker container). To resolve it, we'll need to `--user $(id -u):$(id -g)` in the docker command, as suggested in [here](https://dille.name/blog/2018/07/16/handling-file-permissions-when-writing-to-volumes-from-docker-containers/).
* Resource quota(?)
	* If the existing jobs on a slave machine has taking out all the resource quota, new jobs will be blocked to send to that machine. Not sure if that is needed, as we can also use slave CPU percentage to block sending new jobs.
	* No need to define what kind of slaves (CPU core, ...) the job want to run at, since slaves should be just multi-core boxes with multiple jobs running on it (otherwise we cannot autoscale them based on CPU usage).

TODO: Job specific input parameters.

It doesn't matter if the job config (in repo) and infrastructure-as-code are in the same production repo, as they are with separated deployment anyway (same as whether app code and AWS setup are in the same repo or not, so may apply the same rule for both of them). However, job configuration should probably go in the same place as where the production infrastructure-as-code in.

We should completely hide docker operations (especially forbid volume link) so docker can make sure each run can be cleaned up completely.

#### Slaves

Slaves should be a customized extension of [docker](https://hub.docker.com/_/docker) image with slave agent burned in. Slave agent talks to docker by [Java Docker API Client](https://github.com/docker-java/docker-java).

There's no need to enable SSH in the slave box, as communication between master API and slave agent is through message broker.

Job are run in docker containers inside of this slave docker container. It is very important that (1) necessary port is opened, and (2) credentials can be passed in, in case the job want to communicate with outside world (e.g. curl/git clone from outside, upload to S3, ...).

##### Share docker caching

Since everything are running on docker, we may consider using [registry](https://hub.docker.com/_/registry/) to share docker cache across hosts/slaves. And if a step is not defined in Dockerfile (e.g. library installation inside of the script), the installation should be down everytime (rather than home baked caching dependencies e.g. [in CircleCI](https://circleci.com/docs/2.0/caching/)). Also refer [here (a 4 years old guide which may be out of date but describe the problem clearly)](https://runnable.com/blog/distributing-docker-cache-across-hosts) and [here (new/updated toolsets)](https://medium.com/titansoft-engineering/docker-build-cache-sharing-on-multi-hosts-with-buildkit-and-buildx-eb8f7005918e).

##### Slave agent

Even if both master and slave has multiple machines (masters are tranditional API machines with a load balancer balances the API calls), we still want them to be seperated machines. Reason:

* Master (working for simple tasks and response user quickly) can use a pure API framework, can keep stateless, easy to re-deploy and killed when necessary.
* Slave overloading will not cause master to freeze/not responding.
* Master and slave can follow different scaling rules.

Roles for master/slave machines:

* Master machine has API server running on it. It is in charge of
	* Expose API endpoints. Manage (create/update/delete) jobs. Serve results of querying historical job run data. Act as the gateway of triggering new job.
	* When there's a new job request, it record it -- create run record in database ("trigger time", ...) with status "in progress", and pass the job information to slave to be executed.
* Slave machine hosts a long-run agent on it. Agent is in charge of
	* Starts the job.
	* Monitors the job execution progress.
	* Upload the console output/testing results to persistent storages(s), and modify the database run record (complete time and status "done") without the needs/detour to communicate to master on this.

Master/slave communication:

* **Triggering:** triggering should be done by message queues. Master sends the task to a message queuing system (may have multiple queues based on "resource quota"). When slave have spear compatibility, it actively goes the message queue to grab messages and work on them. It is a better option than remote calls (like the various options provided by [Spring remoting support](https://docs.spring.io/spring/docs/5.1.9.RELEASE/spring-framework-reference/integration.html#remoting-rmi)).
	* With this loose coupling (by a queueing system), there's no need for master/slave to keep a (SSH/...) connection when a job is executed.
	* It naturally acts as a buffer, so
		* Slave machines are not overloaded.
		* Unstarted runs (if all slaves are busy) are safe when machines restarts/redeploy.
	* It naturally distributes the run tasks to multiple machines, regardless of how many masters/slave machines we have.
	* Open question: How to implement job aborting within message queue infrastructure?
		* A working but tedious approach is master can setup the run to be in `abort` state. And slave, while executing the job, chech the status periodically.
		* Maybe when slave starts the job, it records/communicates to master who it is (saving to database?), so later on master has a way to directly find it, and send kill signal to it.
			* Not ideal because it is a very different communication protocol which needs a separate setup.
			* Since slave host machines and docker nodes are both disposable, it doesn't make many sense to save their identities.
* **Share/communicate job status:** By the database.
	* No need for slave agent (as client) to communicate back to master (server) to notify the job is done.
		* Master is supposed to be stateless. No need for it to keep the job status in its memory so no need for slave to notify it.
	* Everytime the run status is queried, master should consult the database. Master may cache the "done" case since it is forever done. It should be explicitly marked in the caching policy that "in progress" is not a cachable state.

Slave agent can be an application burn into the slave image (an extension of `docker` image as described above -- not the container the actual job is running).

* [Jenkins is using a different approach]((https://wiki.jenkins.io/display/JENKINS/SSH+Slaves+plugin)) to `scp` the slave agent every single time a new job starts (to resolve the problem of legacy agent version) by overwriting the agent is shared by all jobs in slave. That's mostly because Jenkins slave machines (setup by using manually and stays persistently) have configuration drafting. We have no need to do it, as our slaves (as docker containers) are disposable, and can be cleaned up every time we want to upgrade the slave agent version.
* This also saves the need for api master to know `scp`/[Apache MINA SSHD](https://mina.apache.org/sshd-project/).

Slave is very likely to be implemented in [RabbitMQ](https://spring.io/guides/gs/messaging-rabbitmq/) and [Spring Cloud Stream](https://spring.io/projects/spring-cloud-stream) ([this article](http://pillopl.github.io/testing-messaging/) (originally about E2E testing) illustrated a good implementation). Slave should grab/distribute tasks based on a combined concern of CPU and resource quota.

* Slave which has CPU below some threshold (+ not notified to be graceful shutdown) should grab new tasks.
	* Cons:
		* Potentially risky if a job use significant different amount of resources in different stage of it. CPU may goes up unexpectedly and finally freeze that slave box/affects all jobs running on it.
			* Need to know the detail of how docker manage/distribute resources for multiple containers running on the same box.
			* For example, a testing job which uses single thread for initialization, and execute tests using multiple CPUs in parallel.
		* Will use, and highly tight to backend infrastructure: load balancer in orchestration framework.
* Slave which has certain amount of unused resource quota (+ not notified to be graceful shutdown) should grab new tasks.
	* Cons:
		* Resource quota is a customized business logic, which means we need to implement the load balancing logic in our master/slave agents: master consults all slave agents about their remaining quota, and then choose one from them.
		* If user mis-configured the resource quota to be too small, particular job may consume to many resources, and finally caused the slave machine to be freeze/in unhealthy state.
* To prevent the cons, slave should only grab new tasks when *both* of the above conditions meet.
* The lowest always need to be above some threshold, because otherwise autoscaling mechanism is very hard to scale down as no node is completely idle.

Slave auto-scaling:

* There should be multiple tasks running in the same (resource-rich) slave machine, rather than one machine per job.
	* Pros:
		* Various job running together in a single hosting machine can smooth CPU and other resources usage.
		* Scale up and down can be less sparky (not sure if this can be done by a smart auto-scaling policy).
	* Cons:
		* Runs are not independent. A bad run may overload the machine and affect other jobs running on it (depending on how docker distribute hardware resources).
	* Slave agent need to be able to create job-specific docker containers, and execute the job inside of it, and cleanup the container after it. May consider using [Java Docker API Client](https://github.com/docker-java/docker-java).
		* Slave agent should be able to manage multiple jobs to be executed together in the same machine. See below "autoscaling" for reasons.
* Graceful shutdown (scale down only happens when the agent finish all jobs in hands) is a prerequisite for auto-scaling.
	* Cannot use retry/rerun a job as a workaround, as
		* The job (e.g. deployment) may not be idempotent.
		* Users are urgently waiting for the result.
	* Need to send notification to a slave machine for graceful shutdown, and only do it after it finished all tasks.
		* Not sure if that can be supported by an existing infrastructure (which is mostly for killing a machine if anything may go wrong -- health check endpoint/...).
* Auto-scaling policy:
	* CPU usage:
		* May be not compatible with resource quota, as a job may have high resource quota, but use only a little bit of CPU in some phase of it.
		* May conflict to the decision when a slave should grab new jobs.
	* The length of message queue ([SQS + AWS](https://docs.aws.amazon.com/autoscaling/ec2/userguide/as-using-sqs-queue.html) supports that):
		* When there's a queue (so the policy has useful input), that means everybody is waiting to be kicked off.
		* May cause some important (e.g. fire-fighting) jobs to not being kicked off immediately.
	* May use the combination of the two.
		* Not sure if that can be supported by an existing infrastructure.
* Slave auto-scaling should be implemented (probably through Kubernetes).

#### Git fetching

For Git repo jobs, repo needs to be fetched *two* times, once from master and once from slave (or master need to send content to slave through RPC/scp). To minimize network overhead, master fetches the single config file (to know the job name, server type, and other metadata), while slave fetches the whole repo. Slave git fetch should be inside of the docker container rather than the host machine (so we know it is completely gone when the container is destroyed).

Note that while [single branch clone](https://stackoverflow.com/questions/1778088/how-do-i-clone-a-single-branch-in-git/7034921#7034921) is supported by GitHub, [single commit fetch](https://stackoverflow.com/a/30701724/11335489) is only supported by GitLab but not GitHub. This fact may limits our git operations performance

Also, if we want to run a task only based on the what is changed from git, we'll need to fetch more than one commit which is a more complicated task.

#### Results where to?

Results should be queried from API endpoint, for example `/jobs/123/tasks/456/results/console` or `/job/123/tasks/456/results/junit` or ... May provide `/jobs/123/results` with JSON return type to provide information on what kind of result that particular job has. Detailed return results can be in [different IANA context-type](https://www.iana.org/assignments/media-types/media-types.xhtml), e.g. JUnit reports in `application/zip` of a folder of XML files. May use some data lake solutions (e.g. Hadoop HDFS (prefer) or AWS S3) to save those result files. Parsing/pretty e.g. JUnit report is completely a frontend plugin.

#### Pipeline where to?

Pipeline should be a client side setup/setup in a layer on top of the RESTful API layer, as it breaks independency between jobs/RESTful endpoints. It can be implemented as [batch operation](https://www.codementor.io/blog/batch-endpoints-6olbjay1hd).

* Pipeline in UI client is obvious/through common sense.
* Command line client shouldn't have shared pipeline. User can have their shellscript with multiple steps.
* There may be an pipeline extension GitHub PR hooks client (for deployment after a master commit).
  * But this makes the GitHub PR hooks client not universally applied, but depend on indivitual job. Better approach?

In case pipeline definition is in code (not necessarily to be in the same repo as the endpoint job), consider the pipeline layer git fetch a single file from [a single commit](https://stackoverflow.com/a/30701724/11335489) (GitHub does not support, GitLab supports) or [a single branch](https://stackoverflow.com/questions/1778088/how-do-i-clone-a-single-branch-in-git/7034921#7034921) (GitHub supports). Then each step/endpoint git fetch seperately.

```
git clone <source> -b <branch-name> --single-branch --depth 1
```

```
git init
git fetch --depth=1 <source>
git checkout -f <commit-sha>
```

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

## References

* [Jenkins distributed builds](https://wiki.jenkins.io/display/JENKINS/Distributed+builds)
