#!/bin/bash
set -euxo pipefail

sed -i'.original' -e "s/spring.profiles.active=dev/spring.profiles.active=docker/g" master-api/src/main/resources/application.properties
sed -i'.original' -e "s/spring.profiles.active=dev/spring.profiles.active=docker/g" master-slave-shared/src/main/resources/application.properties
sed -i'.original' -e "s/spring.profiles.active=dev/spring.profiles.active=docker/g" slave-agent/src/main/resources/application.properties

trap_func () {
  sed -i'.original' -e "s/spring.profiles.active=docker/spring.profiles.active=dev/g" master-api/src/main/resources/application.properties
  sed -i'.original' -e "s/spring.profiles.active=docker/spring.profiles.active=dev/g" master-slave-shared/src/main/resources/application.properties
  sed -i'.original' -e "s/spring.profiles.active=docker/spring.profiles.active=dev/g" slave-agent/src/main/resources/application.properties
}

trap trap_func EXIT

mvn package -Dskip.unittest=true -Dskip.it=true

docker-compose rm -v -f database
docker-compose build
