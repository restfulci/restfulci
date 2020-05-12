CREATE TABLE pipeline (
  id serial PRIMARY KEY,
  name text NOT NULL UNIQUE
);

CREATE TABLE referred_job (
  id serial PRIMARY KEY,
  pipeline_id serial REFERENCES pipeline(id) ON DELETE CASCADE,
  original_job_id integer NOT NULL
);

CREATE TABLE referred_job_dependency (
  upstream_referred_job_id serial REFERENCES referred_job(id) ON DELETE CASCADE,
  downstream_referred_job_id serial REFERENCES referred_job(id) ON DELETE CASCADE
);

CREATE TABLE cycle (
  id serial PRIMARY KEY,
  pipeline_id serial REFERENCES pipeline(id) ON DELETE CASCADE,
  trigger_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  complete_at timestamp
);

CREATE TABLE referred_run (
  id serial PRIMARY KEY,
  cycle_id serial REFERENCES cycle(id) ON DELETE CASCADE,
  original_job_id integer NOT NULL,
  original_run_id integer NOT NULL,
  phase_shortname char(1) NOT NULL CHECK (
    phase_shortname='N' OR
    phase_shortname='I' OR
    phase_shortname='C' OR
    phase_shortname='A') DEFAULT 'N',
  exit_code integer
);

CREATE TABLE referred_run_dependency (
  upstream_referred_run_id serial REFERENCES referred_run(id) ON DELETE CASCADE,
  downstream_referred_run_id serial REFERENCES referred_run(id) ON DELETE CASCADE
);
