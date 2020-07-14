CREATE TABLE pipeline (
  id serial PRIMARY KEY,
  name text NOT NULL UNIQUE
);

CREATE TABLE parameter (
  id serial PRIMARY KEY,
  pipeline_id serial REFERENCES pipeline(id) ON DELETE CASCADE,
  name text NOT NULL CHECK (name ~ '^[A-Z_][A-Z0-9_]*$'),
  default_value text,
  choices text[]
);

CREATE TABLE referred_job (
  id serial PRIMARY KEY,
  pipeline_id serial REFERENCES pipeline(id) ON DELETE CASCADE,
  original_job_id integer NOT NULL
);

-- TODO:
-- We need to build an "or" relationship for `parameter` and
-- `parameter_map`, to handle git remote job, for which we need
-- to pass either `branchName` or `commitSha`.
CREATE TABLE parameter_map (
  id serial PRIMARY KEY,
  referred_job_id serial REFERENCES referred_job(id) ON DELETE CASCADE,
  parameter_id integer REFERENCES parameter(id) ON DELETE RESTRICT,
  remote_name text NOT NULL,
  optional boolean NOT NULL
);

CREATE TABLE referred_job_dependency (
  upstream_referred_job_id serial REFERENCES referred_job(id) ON DELETE CASCADE,
  downstream_referred_job_id serial REFERENCES referred_job(id) ON DELETE CASCADE,
  PRIMARY KEY (upstream_referred_job_id, downstream_referred_job_id)
);

CREATE TABLE cycle (
  id serial PRIMARY KEY,
  pipeline_id serial REFERENCES pipeline(id) ON DELETE CASCADE,
  status_shortname char(1) NOT NULL CHECK (
    status_shortname='I' OR
    status_shortname='S' OR
    status_shortname='F' OR
    status_shortname='A') DEFAULT 'I',
  unfinalized_status_shortname char(1) CHECK (
    unfinalized_status_shortname='S' OR
    unfinalized_status_shortname='F' OR
    unfinalized_status_shortname='A') DEFAULT 'S',
  trigger_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  complete_at timestamp
);

CREATE TABLE input (
  id serial PRIMARY KEY,
  cycle_id serial REFERENCES cycle(id) ON DELETE CASCADE,
  name text NOT NULL CHECK (name ~ '^[A-Z_][A-Z0-9_]*$'),
  value text
);

CREATE TABLE referred_run (
  id serial PRIMARY KEY,
  cycle_id serial REFERENCES cycle(id) ON DELETE CASCADE,
  original_job_id integer NOT NULL,
  original_run_id integer,
  status_shortname char(1) NOT NULL CHECK (
    status_shortname='N' OR
    status_shortname='I' OR
    status_shortname='S' OR
    status_shortname='F' OR
    status_shortname='E' OR
    status_shortname='K' OR
    status_shortname='A') DEFAULT 'N',
  error_message text,
  exit_code integer
);

CREATE TABLE input_map (
  id serial PRIMARY KEY,
  referred_run_id serial REFERENCES referred_job(id) ON DELETE CASCADE,
  input_id integer REFERENCES input(id) ON DELETE RESTRICT,
  remote_name text NOT NULL
);

CREATE TABLE referred_run_dependency (
  upstream_referred_run_id serial REFERENCES referred_run(id) ON DELETE CASCADE,
  downstream_referred_run_id serial REFERENCES referred_run(id) ON DELETE CASCADE
);
