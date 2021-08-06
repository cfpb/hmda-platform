--
-- PostgreSQL database dump
--

-- Dumped from database version 10.6
-- Dumped by pg_dump version 10.5

-- Started on 2019-06-18 12:25:03 EDT

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
-- TOC entry 293 (class 1259 OID 60865)
-- Name: institutions2019; Type: TABLE; Schema: hmda_user; Owner: hmda_user
--

CREATE TABLE hmda_user.institutions2019 (
    lei character varying NOT NULL,
    activity_year integer NOT NULL,
    agency integer NOT NULL,
    institution_type integer NOT NULL,
    id2017 character varying NOT NULL,
    tax_id character varying NOT NULL,
    rssd integer NOT NULL,
    respondent_name character varying NOT NULL,
    respondent_state character varying NOT NULL,
    respondent_city character varying NOT NULL,
    parent_id_rssd integer NOT NULL,
    parent_name character varying NOT NULL,
    assets integer NOT NULL,
    other_lender_code integer NOT NULL,
    topholder_id_rssd integer NOT NULL,
    topholder_name character varying NOT NULL,
    hmda_filer boolean NOT NULL,
    quarterly_filer boolean default false NOT NULL,
    quarterly_filer_has_filed boolean default false NOT NULL

);

ALTER TABLE hmda_user.institutions2019
    ADD COLUMN notes text not null default '';
ALTER TABLE hmda_user.institutions2019
    ALTER COLUMN notes DROP DEFAULT;
ALTER TABLE hmda_user.institutions2019 ADD COLUMN created_at TIMESTAMP;
ALTER TABLE hmda_user.institutions2019 ALTER COLUMN created_at SET DEFAULT now();

ALTER TABLE hmda_user.institutions2019 ADD COLUMN updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE hmda_user.institutions2019 OWNER TO hmda_user;



--
-- TOC entry 5447 (class 2606 OID 60872)
-- Name: institutions2019 institutions2019_pkey; Type: CONSTRAINT; Schema: hmda_user; Owner: hmda_user
--

ALTER TABLE ONLY hmda_user.institutions2019
    ADD CONSTRAINT institutions2019_pkey PRIMARY KEY (lei);


-- Completed on 2019-06-18 12:25:04 EDT

--
-- PostgreSQL database dump complete
--

