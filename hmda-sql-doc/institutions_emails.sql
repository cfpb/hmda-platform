--
-- PostgreSQL database dump
--

-- Dumped from database version 10.6
-- Dumped by pg_dump version 10.5

-- Started on 2018-06-18 13:13:03 EDT

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
-- TOC entry 280 (class 1259 OID 20226)
-- Name: institutions_emails; Type: TABLE; Schema: hmda_user; Owner: hmda_user
--

CREATE TABLE hmda_beta_user.institutions_emails (
    id integer NOT NULL,
    lei character varying NOT NULL,
    email_domain character varying NOT NULL
);


ALTER TABLE hmda_beta_user.institutions_emails OWNER TO hmda_user;

--
-- TOC entry 279 (class 1259 OID 20224)
-- Name: institutions_emails_id_seq; Type: SEQUENCE; Schema: hmda_user; Owner: hmda_user
--

CREATE SEQUENCE hmda_beta_user.institutions_emails_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE hmda_beta_user.institutions_emails_id_seq OWNER TO hmda_user;

--
-- TOC entry 5614 (class 0 OID 0)
-- Dependencies: 279
-- Name: institutions_emails_id_seq; Type: SEQUENCE OWNED BY; Schema: hmda_user; Owner: hmda_user
--

ALTER SEQUENCE hmda_beta_user.institutions_emails_id_seq OWNED BY hmda_beta_user.institutions_emails.id;


--
-- TOC entry 5477 (class 2604 OID 20229)
-- Name: institutions_emails id; Type: DEFAULT; Schema: hmda_user; Owner: hmda_user
--

ALTER TABLE ONLY hmda_beta_user.institutions_emails ALTER COLUMN id SET DEFAULT nextval('hmda_beta_user.institutions_emails_id_seq'::regclass);


--
-- TOC entry 5479 (class 2606 OID 20234)
-- Name: institutions_emails institutions_emails_pkey; Type: CONSTRAINT; Schema: hmda_user; Owner: hmda_user
--

ALTER TABLE ONLY hmda_beta_user.institutions_emails
    ADD CONSTRAINT institutions_emails_pkey PRIMARY KEY (id);
