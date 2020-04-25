#!/bin/bash
set -euxo pipefail

mvn clean package -Dskip.unittest=true -Dskip.it=true

docker-compose rm -v -f database
docker-compose build
