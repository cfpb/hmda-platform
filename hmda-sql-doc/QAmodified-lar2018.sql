
--

-- Dumped from database version 10.6
-- Dumped by pg_dump version 10.5

-- Started on 2019-06-05 11:44:49 EDT

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
-- Name: qa_mlar_table_2018; Type: TABLE; Schema: hmda_user; Owner: hmda_user
--
CREATE TABLE hmda_user.qa_mlar_table_2018
(
 id integer NOT NULL,
    lei character varying COLLATE pg_catalog."default" NOT NULL,
    loan_type integer,
    loan_purpose integer,
    preapproval integer,
    construction_method character varying COLLATE pg_catalog."default",
    occupancy_type integer,
    loan_amount numeric,
    action_taken_type integer,
    state character varying COLLATE pg_catalog."default",
    county character varying COLLATE pg_catalog."default",
    tract character varying COLLATE pg_catalog."default",
    ethnicity_applicant_1 character varying COLLATE pg_catalog."default",
    ethnicity_applicant_2 character varying COLLATE pg_catalog."default",
    ethnicity_applicant_3 character varying COLLATE pg_catalog."default",
    ethnicity_applicant_4 character varying COLLATE pg_catalog."default",
    ethnicity_applicant_5 character varying COLLATE pg_catalog."default",
    ethnicity_observed_applicant integer,
    ethnicity_co_applicant_1 character varying COLLATE pg_catalog."default",
    ethnicity_co_applicant_2 character varying COLLATE pg_catalog."default",
    ethnicity_co_applicant_3 character varying COLLATE pg_catalog."default",
    ethnicity_co_applicant_4 character varying COLLATE pg_catalog."default",
    ethnicity_co_applicant_5 character varying COLLATE pg_catalog."default",
    ethnicity_observed_co_applicant integer,
    race_applicant_1 character varying COLLATE pg_catalog."default",
    race_applicant_2 character varying COLLATE pg_catalog."default",
    race_applicant_3 character varying COLLATE pg_catalog."default",
    race_applicant_4 character varying COLLATE pg_catalog."default",
    race_applicant_5 character varying COLLATE pg_catalog."default",
    race_co_applicant_1 character varying COLLATE pg_catalog."default",
    race_co_applicant_2 character varying COLLATE pg_catalog."default",
    race_co_applicant_3 character varying COLLATE pg_catalog."default",
    race_co_applicant_4 character varying COLLATE pg_catalog."default",
    race_co_applicant_5 character varying COLLATE pg_catalog."default",
    race_observed_applicant integer,
    race_observed_co_applicant integer,
    sex_applicant integer,
    sex_co_applicant integer,
    observed_sex_applicant integer,
    observed_sex_co_applicant integer,
    age_applicant character varying COLLATE pg_catalog."default",
    applicant_age_greater_than_62 character varying COLLATE pg_catalog."default",
    age_co_applicant character varying COLLATE pg_catalog."default",
    coapplicant_age_greater_than_62 character varying COLLATE pg_catalog."default",
    income character varying COLLATE pg_catalog."default",
    purchaser_type integer,
    rate_spread character varying COLLATE pg_catalog."default",
    hoepa_status integer,
    lien_status integer,
    credit_score_type_applicant integer,
    credit_score_type_co_applicant integer,
    denial_reason1 integer,
    denial_reason2 integer,
    denial_reason3 integer,
    denial_reason4 integer,
    total_loan_costs character varying COLLATE pg_catalog."default",
    total_points character varying COLLATE pg_catalog."default",
    origination_charges character varying COLLATE pg_catalog."default",
    discount_points character varying COLLATE pg_catalog."default",
    lender_credits character varying COLLATE pg_catalog."default",
    interest_rate character varying COLLATE pg_catalog."default",
    payment_penalty character varying COLLATE pg_catalog."default",
    debt_to_incode character varying COLLATE pg_catalog."default",
    loan_value_ratio character varying COLLATE pg_catalog."default",
    loan_term character varying COLLATE pg_catalog."default",
    rate_spread_intro character varying COLLATE pg_catalog."default",
    baloon_payment integer,
    insert_only_payment integer,
    amortization integer,
    other_amortization integer,
    property_value character varying COLLATE pg_catalog."default",
    home_security_policy integer,
    lan_property_interest integer,
    total_units character varying COLLATE pg_catalog."default",
    mf_affordable character varying COLLATE pg_catalog."default",
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
    population character varying COLLATE pg_catalog."default",
    minority_population_percent character varying COLLATE pg_catalog."default",
    ffiec_med_fam_income character varying COLLATE pg_catalog."default",
    tract_to_msamd character varying COLLATE pg_catalog."default",
    owner_occupied_units character varying COLLATE pg_catalog."default",
    one_to_four_fam_units character varying COLLATE pg_catalog."default",
    msa_md integer,
    loan_flag character varying COLLATE pg_catalog."default",
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    submission_id character varying COLLATE pg_catalog."default",
    msa_md_name character varying COLLATE pg_catalog."default",
    filing_year integer,
    conforming_loan_limit character varying COLLATE pg_catalog."default",
    median_age integer,
    median_age_calculated character varying COLLATE pg_catalog."default",
    median_income_percentage numeric,
    race_categorization character varying COLLATE pg_catalog."default",
    sex_categorization character varying COLLATE pg_catalog."default",
    ethnicity_categorization character varying COLLATE pg_catalog."default",
    uniq_id integer ,
    percent_median_msa_income character varying COLLATE pg_catalog."default",
    dwelling_category character varying COLLATE pg_catalog."default",
    loan_product_type character varying COLLATE pg_catalog."default",
    uli character varying COLLATE pg_catalog."default",
    action_taken_date integer,
    checksum character varying COLLATE pg_catalog."default",
    file_name character varying,
    time_stamp bigint
)
WITH (
    OIDS = FALSE
)
TABLESPACE pg_default;

ALTER TABLE hmda_user.qa_mlar_table_2018
    OWNER to hmda_user;

--
-- TOC entry 285 (class 1259 OID 25533)
-- Name: qa_mlar_table_2018_uniq_id_seq; Type: SEQUENCE; Schema: hmda_user; Owner: hmda_user
--
