--
-- PostgreSQL database dump
--

-- Dumped from database version 10.6
-- Dumped by pg_dump version 10.5

-- Started on 2021-06-18 12:21:09 EDT

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
-- Name: qa_ts_prv_table_2021; Type: TABLE; Schema: hmda_user; Owner: hmda_user
--

CREATE TABLE hmda_user.qa_ts_prv_table_2021 (
    id integer NOT NULL,
    institution_name character varying COLLATE pg_catalog."default" NOT NULL,
    year integer,
    quarter integer,
    name character varying COLLATE pg_catalog."default",
    phone character varying COLLATE pg_catalog."default",
    email character varying COLLATE pg_catalog."default",
    street character varying COLLATE pg_catalog."default",
    city character varying COLLATE pg_catalog."default",
    state character varying COLLATE pg_catalog."default",
    zip_code character varying COLLATE pg_catalog."default",
    agency integer,
    total_lines integer,
    tax_id character varying COLLATE pg_catalog."default",
    lei character varying COLLATE pg_catalog."default" NOT NULL,
    created_at timestamp without time zone DEFAULT now(),
    submission_id character varying COLLATE pg_catalog."default",
    is_quarterly boolean NOT NULL DEFAULT false,
    sign_date bigint,
    file_name character varying,
    time_stamp bigint

);


ALTER TABLE hmda_user.qa_ts_prv_table_2021 OWNER TO hmda_user;

--
-- TOC entry 5444 (class 2606 OID 20734)
-- Name: qa_ts_prv_table_2021 ts2021_qa_prv_pkey; Type: CONSTRAINT; Schema: hmda_user; Owner: hmda_user
--

-- Completed on 2021-06-18 12:21:09 EDT

--
-- PostgreSQL database dump complete
--