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

CREATE TABLE run (
  id serial PRIMARY KEY,
  job_id serial REFERENCES job(id) ON DELETE CASCADE,
  -- user_id serial REFERENCES application_user (id) ON DELETE CASCADE,
  trigger_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  complete_at timestamp
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
