# Contributing

## Development environments

### `dev`

* Java, maven, STS, and docker are installed in the local machine.
* Persistent storages are through `docker-compose.dev.yml`.

```
docker-compose -f docker-compose.dev.yml rm -v -f database
docker-compose -f docker-compose.dev.yml up
mvn test
```

### `docker`

```
bash docker_build-mac.sh
docker-compose up
```

Then you can access master API by `localhost:8881`.
