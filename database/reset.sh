export PGPASSWORD=postgres
export PGHOST=localhost
psql -U postgres -w -f cleanup.sql

. setup.sh
