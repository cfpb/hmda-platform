--
-- PostgreSQL database dump
--

-- Dumped from database version 10.6
-- Dumped by pg_dump version 10.5

-- Started on 2019-06-18 12:27:10 EDT

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
-- TOC entry 281 (class 1259 OID 20735)
-- Name: qa_lar_loanlimit_table_2020; Type: TABLE; Schema: hmda_user; Owner: hmda_user
--


CREATE TABLE hmda_user.qa_lar_loanlimit_table_2020 (
    id character varying COLLATE pg_catalog."default",
    name character varying COLLATE pg_catalog."default",
    total_lars integer,
    total_amount bigint,
    conv integer,
    fha integer,
    va integer,
    fsa integer,
    site_built integer,
    manufactured integer,
    one_to_four integer,
    five_plus integer,
    home_purchase integer,
    home_improvement integer,
    refinancing integer,
    cash_out_refinancing integer,
    other_purpose integer,
    not_applicable_purpose integer,
    file_name character varying,
    time_stamp bigint
);


ALTER TABLE hmda_user.qa_lar_loanlimit_table_2020 OWNER TO hmda_user;

-- Completed on 2019-06-18 12:27:11 EDT

--
-- PostgreSQL database dump complete
--