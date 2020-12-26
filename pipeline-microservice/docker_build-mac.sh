#!/bin/bash
set -euxo pipefail

mvn -f api-cron/pom.xml clean package -Dskip.unittest=true -Dskip.it=true

docker-compose rm -v -f pipeline-postgres
docker-compose build
