--
-- PostgreSQL database dump
--

-- Dumped from database version 10.6
-- Dumped by pg_dump version 10.5

-- Started on 2019-06-18 12:21:09 EDT

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
-- TOC entry 280 (class 1259 OID 20727)
-- Name: transmittalsheet2018; Type: TABLE; Schema: hmda_user; Owner: hmda_user
--

CREATE TABLE hmda_user.transmittalsheet2018 (
    id integer NOT NULL,
    institution_name character varying NOT NULL,
    year integer,
    quarter integer,
    name character varying,
    phone character varying,
    email character varying,
    street character varying,
    city character varying,
    state character varying,
    zip_code character varying,
    agency integer,
    total_lines integer,
    tax_id character varying,
    lei character varying NOT NULL,
    created_at timestamp without time zone DEFAULT now(),
    submission_id character varying,
    is_quarterly boolean DEFAULT false NOT NULL,
    sign_date bigint
);


ALTER TABLE hmda_user.transmittalsheet2018 OWNER TO hmda_user;

--
-- TOC entry 5444 (class 2606 OID 20734)
-- Name: transmittalsheet2018 ts2018_pkey; Type: CONSTRAINT; Schema: hmda_user; Owner: hmda_user
--

ALTER TABLE ONLY hmda_user.transmittalsheet2018
    ADD CONSTRAINT ts2018_pkey PRIMARY KEY (lei);


-- Completed on 2019-06-18 12:21:09 EDT

--
-- PostgreSQL database dump complete
--