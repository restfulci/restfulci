```
gcloud container clusters create restfulci --num-nodes=1
gcloud container clusters get-credentials restfulci

kubectl create -f kubernetes

gcloud container clusters delete restfulci
```
