CREATE TABLE application_user (
	id serial PRIMARY KEY,
	username text NOT NULL UNIQUE,
	password text NOT NULL
);

CREATE TABLE job (
  id serial PRIMARY KEY,
  name text NOT NULL UNIQUE
);

CREATE TABLE freestyle_job (
  id serial PRIMARY KEY REFERENCES job(id) ON DELETE CASCADE,
  docker_image text NOT NULL,
  command text[] NOT NULL
);

CREATE TABLE git_job (
  id serial PRIMARY KEY REFERENCES job(id) ON DELETE CASCADE,
  remote_origin text NOT NULL,
  config_filepath text NOT NULL
);

CREATE TABLE parameter (
  id serial PRIMARY KEY,
  job_id serial REFERENCES job(id) ON DELETE CASCADE,
  name text NOT NULL CHECK (name ~ '^[A-Z_][A-Z0-9_]*$'),
  default_value text,
  choices text[]
);

CREATE TABLE run (
  id serial PRIMARY KEY,
  job_id serial REFERENCES job(id) ON DELETE CASCADE,
  -- user_id serial REFERENCES application_user (id) ON DELETE CASCADE,
  phase_shortname char(1) NOT NULL CHECK (
    phase_shortname='I' OR
    phase_shortname='C' OR
    phase_shortname='A') DEFAULT 'I',
  trigger_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  complete_at timestamp,
  exit_code integer,
  run_output_object_referral text
);

CREATE TABLE run_result (
  id serial PRIMARY KEY,
  run_id serial REFERENCES run(id) ON DELETE CASCADE,
  type text NOT NULL,
  container_path text NOT NULL,
  object_referral text
);

CREATE FUNCTION freestyle_job_id_from_run (integer)
RETURNS integer AS $return_id$
DECLARE return_id integer;
BEGIN
	SELECT freestyle_job.id INTO return_id
  FROM freestyle_job
    INNER JOIN job ON (freestyle_job.id = job.id)
    INNER JOIN run ON (job.id = run.job_id)
	WHERE run.id = $1;
	RETURN return_id;
END;
$return_id$ LANGUAGE plpgsql
IMMUTABLE;

CREATE TABLE freestyle_run (
  id serial PRIMARY KEY REFERENCES run(id) ON DELETE CASCADE,
  CHECK (freestyle_job_id_from_run(id) IS NOT NULL)
);
-- Can check constrain works:
-- INSERT INTO job (name) VALUES ('freestyle_job');
-- INSERT INTO RUN (job_id) VALUES (1);
-- INSERT INTO freestyle_run (id) VALUES (1); -> error out
-- INSERT INTO freestyle_job (id, docker_image, command) VALUES (1, 'x', '{x}');
-- INSERT INTO freestyle_run (id) VALUES (1); -> pass

CREATE FUNCTION git_job_id_from_run (integer)
RETURNS integer AS $return_id$
DECLARE return_id integer;
BEGIN
	SELECT git_job.id INTO return_id
  FROM git_job
    INNER JOIN job ON (git_job.id = job.id)
    INNER JOIN run ON (job.id = run.job_id)
	WHERE run.id = $1;
	RETURN return_id;
END;
$return_id$ LANGUAGE plpgsql
IMMUTABLE;

CREATE TABLE git_run (
  id serial PRIMARY KEY REFERENCES run(id) ON DELETE CASCADE,
  run_configuration_object_referral text,
  CHECK (git_job_id_from_run(id) IS NOT NULL)
);

CREATE TABLE git_branch_run (
  id serial PRIMARY KEY REFERENCES git_run(id) ON DELETE CASCADE,
  branch_name text
);

CREATE TABLE git_commit_run (
  id serial PRIMARY KEY REFERENCES git_run(id) ON DELETE CASCADE,
  commit_sha text
);

CREATE TABLE input (
  id serial PRIMARY KEY,
  run_id serial REFERENCES run(id) ON DELETE CASCADE,
  name text NOT NULL CHECK (name ~ '^[A-Z_][A-Z0-9_]*$'),
  value text
);
