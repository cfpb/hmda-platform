SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET client_min_messages = warning;
SET row_security = off;

SET default_tablespace = '';

SET default_with_oids = false;

CREATE TABLE hmda_user.submission_history (
    lei character varying NOT NULL,
    submission_id character varying NOT NULL,
    sign_date bigint
);


ALTER TABLE hmda_user.submission_history OWNER TO hmda_user;

ALTER TABLE ONLY hmda_user.submission_history
    ADD CONSTRAINT sh_pkey PRIMARY KEY (lei, submission_id);