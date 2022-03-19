docker system prune
docker system prune --volumes

docker-compose -f docker-compose.upstream.yml rm -v -f keycloak-postgres
docker-compose -f docker-compose.upstream.yml rm -v -f job-postgres
docker-compose -f docker-compose.upstream.yml rm -v -f job-rabbitmq
docker-compose -f docker-compose.upstream.yml rm -v -f pipeline-postgres
