#!/bin/bash
set -euxo pipefail

export PGPASSWORD=postgres
export PGHOST=localhost
psql -U postgres -c "CREATE DATABASE restfulci OWNER postgres;"
psql -U postgres -d restfulci -w -f setup.sql
