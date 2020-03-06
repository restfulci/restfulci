#!/bin/bash
set -e

# Workaround for
# https://github.com/docker-library/postgres/issues/474
# https://github.com/docker-library/postgres/pull/440
pg_ctl -o "-c listen_addresses='localhost'" -w restart

export PGPASSWORD=postgres
export PGHOST=localhost
psql -U postgres -c "CREATE DATABASE restfulci OWNER postgres;"
psql -U postgres -d restfulci -w -f /database/setup.sql
