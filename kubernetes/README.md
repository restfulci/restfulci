```
gcloud container clusters create restfulci --num-nodes=2
gcloud container clusters get-credentials restfulci

kubectl create -f kubernetes --save-config
kubectl apply -f kubernetes

gcloud container clusters delete restfulci
```

```
kubectl delete --all pods --namespace=default
kubectl delete --all deployments --namespace=default
kubectl delete --all services --namespace=default
kubectl delete --all configmaps --namespace=default
kubectl delete --all persistentvolumeclaims --namespace=default
```
