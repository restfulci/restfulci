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

CREATE TABLE git_run (
  id serial PRIMARY KEY REFERENCES run(id) ON DELETE CASCADE
);

CREATE TABLE git_branch_run (
  id serial PRIMARY KEY REFERENCES git_run(id) ON DELETE CASCADE,
  branch_name text
);

CREATE TABLE git_commit_run (
  id serial PRIMARY KEY REFERENCES git_run(id) ON DELETE CASCADE,
  commit_sha text
);
