# Contributing

## Local development environments

### `dev`

* Java, maven, STS, and docker are installed in the local machine.
* Persistent storages are through `docker-compose.dev.yml`.

In each microservice folder

```
docker-compose -f docker-compose.dev.yml rm -v -f postgres
docker-compose -f docker-compose.dev.yml up
```

Tests:

* From STS: No special setup needed.
* From command line: `mvn verify`

Run server:

* From STS: Need to setup environment variable `SPRING_PROFILES_ACTIVE=dev` in "Run configurations" of `*Application.java`. Then the service should be available from `localhost:8080`.

### `docker`

```
cd job-microservice
bash docker_build-mac.sh
docker-compose up
```

Then you can access job microservice API by `localhost:8881`.

```
cd pipeline-microservice
bash docker_build-mac.sh
docker-compose up
```

Then you can access pipeline microservice API by `localhost:8882` (with mocked job microservice).

## Deployment

### GKE/Skaffold

```
gcloud container clusters create restfulci --num-nodes=6
gcloud container clusters get-credentials restfulci

gcloud container clusters delete restfulci
```

```
skaffold run
skaffold delete
```

```
kubectl create -f kubernetes --save-config
kubectl apply -f kubernetes

kubectl delete --all pods --namespace=default
kubectl delete --all deployments --namespace=default
kubectl delete --all services --namespace=default
kubectl delete --all configmaps --namespace=default
kubectl delete --all persistentvolumeclaims --namespace=default
```
