--
-- PostgreSQL database dump
--

-- Dumped from database version 10.6
-- Dumped by pg_dump version 10.5

-- Started on 2019-06-18 12:31:41 EDT

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

--
-- TOC entry 283 (class 1259 OID 22247)
-- Name: submission_history; Type: TABLE; Schema: hmda_user; Owner: hmda_user
--

CREATE TABLE hmda_user.submission_history (
    lei character varying NOT NULL,
    submission_id character varying NOT NULL,
    sign_date bigint
);


ALTER TABLE hmda_user.submission_history OWNER TO hmda_user;

--
-- TOC entry 5451 (class 2606 OID 22254)
-- Name: submission_history sh2018_pkey; Type: CONSTRAINT; Schema: hmda_user; Owner: hmda_user
--

ALTER TABLE ONLY hmda_user.submission_history
    ADD CONSTRAINT sh2018_pkey PRIMARY KEY (lei, submission_id);


-- Completed on 2019-06-18 12:31:42 EDT

--
-- PostgreSQL database dump complete
--

