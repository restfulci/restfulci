CREATE TABLE application_user (
	id serial PRIMARY KEY,
	username text NOT NULL UNIQUE,
	password text NOT NULL
);

CREATE TABLE job (
  id serial PRIMARY KEY,
  name text NOT NULL UNIQUE
);

CREATE TABLE run (
  id serial PRIMARY KEY,
  job_id serial REFERENCES job (id) ON DELETE CASCADE,
  user_id serial REFERENCES application_user (id) ON DELETE CASCADE,
  trigger_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  complete_at timestamp
);
