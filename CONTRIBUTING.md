# Contributing

## Local development environments

### `dev`

* Java, maven, STS, and docker are installed in the local machine.
* Persistent storages are through `docker-compose.dev.yml`.

```
docker-compose -f docker-compose.dev.yml rm -v -f postgres
docker-compose -f docker-compose.dev.yml up
mvn test
```

### `docker`

```
cd job
bash docker_build-mac.sh
docker-compose up
```

Then you can access master API by `localhost:8881`.

## Deployment

### GKE/Skaffold

```
gcloud container clusters create restfulci --num-nodes=4
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
