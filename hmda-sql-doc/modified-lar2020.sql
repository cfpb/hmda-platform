-- SCHEMA: hmda_user

-- DROP SCHEMA hmda_user ;

CREATE SCHEMA hmda_user AUTHORIZATION hmda_user;

--
-- PostgreSQL database dump
--

-- Dumped from database version 10.6
-- Dumped by pg_dump version 10.5

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
-- TOC entry 282 (class 1259 OID 22057)
-- Name: modifiedlar2020; Type: TABLE; Schema: hmda_user; Owner: hmda_user
--

CREATE TABLE hmda_user.modifiedlar2020 (
    id integer NOT NULL,
    uniq_id integer NOT NULL,
    lei character varying NOT NULL,
    loan_type integer,
    loan_purpose integer,
    preapproval integer,
    construction_method character varying,
    occupancy_type integer,
    loan_amount numeric,
    action_taken_type integer,
    state character varying,
    county character varying,
    tract character varying,
    ethnicity_applicant_1 character varying,
    ethnicity_applicant_2 character varying,
    ethnicity_applicant_3 character varying,
    ethnicity_applicant_4 character varying,
    ethnicity_applicant_5 character varying,
    ethnicity_observed_applicant integer,
    ethnicity_co_applicant_1 character varying,
    ethnicity_co_applicant_2 character varying,
    ethnicity_co_applicant_3 character varying,
    ethnicity_co_applicant_4 character varying,
    ethnicity_co_applicant_5 character varying,
    ethnicity_observed_co_applicant integer,
    race_applicant_1 character varying,
    race_applicant_2 character varying,
    race_applicant_3 character varying,
    race_applicant_4 character varying,
    race_applicant_5 character varying,
    race_co_applicant_1 character varying,
    race_co_applicant_2 character varying,
    race_co_applicant_3 character varying,
    race_co_applicant_4 character varying,
    race_co_applicant_5 character varying,
    race_observed_applicant integer,
    race_observed_co_applicant integer,
    sex_applicant integer,
    sex_co_applicant integer,
    observed_sex_applicant integer,
    observed_sex_co_applicant integer,
    age_applicant character varying,
    applicant_age_greater_than_62 character varying,
    age_co_applicant character varying,
    coapplicant_age_greater_than_62 character varying,
    income character varying,
    purchaser_type integer,
    rate_spread character varying,
    hoepa_status integer,
    lien_status integer,
    credit_score_type_applicant integer,
    credit_score_type_co_applicant integer,
    denial_reason1 integer,
    denial_reason2 integer,
    denial_reason3 integer,
    denial_reason4 integer,
    total_loan_costs character varying,
    total_points character varying,
    origination_charges character varying,
    discount_points character varying,
    lender_credits character varying,
    interest_rate character varying,
    payment_penalty character varying,
    debt_to_incode character varying,
    loan_value_ratio character varying,
    loan_term character varying,
    rate_spread_intro character varying,
    baloon_payment integer,
    insert_only_payment integer,
    amortization integer,
    other_amortization integer,
    property_value character varying,
    home_security_policy integer,
    lan_property_interest integer,
    total_units character varying,
    mf_affordable character varying,
    application_submission integer,
    payable integer,
    aus1 integer,
    aus2 integer,
    aus3 integer,
    aus4 integer,
    aus5 integer,
    reverse_mortgage integer,
    line_of_credits integer,
    business_or_commercial integer,
    population character varying,
    minority_population_percent character varying,
    ffiec_med_fam_income character varying,
    tract_to_msamd character varying,
    owner_occupied_units character varying,
    one_to_four_fam_units character varying,
    msa_md integer,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    submission_id character varying,
    msa_md_name character varying,
    filing_year integer,
    conforming_loan_limit character varying,
    median_age integer,
    median_age_calculated character varying,
    median_income_percentage decimal,
    race_categorization character varying,
    sex_categorization character varying,
    ethnicity_categorization character varying,
    percent_median_msa_income character varying,
    dwelling_category character varying,
    loan_product_type character varying
);


ALTER TABLE hmda_user.modifiedlar2020 OWNER TO hmda_user;

--
-- TOC entry 285 (class 1259 OID 25533)
-- Name: modifiedlar2020_uniq_id_seq; Type: SEQUENCE; Schema: hmda_user; Owner: hmda_user
--

CREATE SEQUENCE hmda_user.modifiedlar2020_uniq_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE hmda_user.modifiedlar2020_uniq_id_seq OWNER TO hmda_user;

--
-- TOC entry 5570 (class 0 OID 0)
-- Dependencies: 285
-- Name: modifiedlar2020_uniq_id_seq; Type: SEQUENCE OWNED BY; Schema: hmda_user; Owner: hmda_user
--

ALTER SEQUENCE hmda_user.modifiedlar2020_uniq_id_seq OWNED BY hmda_user.modifiedlar2020.uniq_id;


--
-- TOC entry 5433 (class 2604 OID 57438)
-- Name: modifiedlar2020 uniq_id; Type: DEFAULT; Schema: hmda_user; Owner: hmda_user
--

ALTER TABLE ONLY hmda_user.modifiedlar2020 ALTER COLUMN uniq_id SET DEFAULT nextval('hmda_user.modifiedlar2020_uniq_id_seq'::regclass);


--
-- TOC entry 5436 (class 2606 OID 25537)
-- Name: modifiedlar2020 modifiedlar2020_pkey; Type: CONSTRAINT; Schema: hmda_user; Owner: hmda_user
--

ALTER TABLE ONLY hmda_user.modifiedlar2020
    ADD CONSTRAINT modifiedlar2020_pkey PRIMARY KEY (uniq_id);


--
-- TOC entry 5434 (class 1259 OID 58733)
-- Name: modifiedlar2020_lei_idx; Type: INDEX; Schema: hmda_user; Owner: hmda_user
--

CREATE INDEX modifiedlar2020_lei_idx ON hmda_user.modifiedlar2020 USING btree (lei);

alter table hmda_user.modifiedlar2020
add column uli character varying;
alter table hmda_user.modifiedlar2020 add column checksum varchar;

-- Completed on 2020-06-05 11:44:50 EDT


--
-- PostgreSQL database dump complete
--