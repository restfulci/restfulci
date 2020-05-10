```
gcloud container clusters create restfulci --num-nodes=3
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
