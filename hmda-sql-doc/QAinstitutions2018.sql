-- Table: hmda_user.qa_panel_table_2018

-- DROP TABLE hmda_user.qa_panel_table_2018;

CREATE TABLE hmda_user.qa_panel_table_2018
(
    lei character varying COLLATE pg_catalog."default",
    activity_year integer,
    agency integer,
    institution_type integer,
    id2017 character varying COLLATE pg_catalog."default",
    tax_id character varying COLLATE pg_catalog."default",
    rssd integer,
    respondent_name character varying COLLATE pg_catalog."default",
    respondent_state character varying COLLATE pg_catalog."default",
    respondent_city character varying COLLATE pg_catalog."default",
    parent_id_rssd integer,
    parent_name character varying COLLATE pg_catalog."default",
    assets integer,
    other_lender_code integer,
    topholder_id_rssd integer,
    topholder_name character varying COLLATE pg_catalog."default",
    hmda_filer boolean,
    quarterly_filer boolean,
    quarterly_filer_has_filed_q1 boolean,
    quarterly_filer_has_filed_q2 boolean,
    quarterly_filer_has_filed_q3 boolean,
    notes character varying COLLATE pg_catalog."default",
    email_domains character varying COLLATE pg_catalog."default",
        file_name character varying,
        time_stamp bigint
 )
WITH (
    OIDS = FALSE
)
TABLESPACE pg_default;

ALTER TABLE hmda_user.qa_panel_table_2018
    OWNER to hmda_user;

GRANT ALL ON TABLE hmda_user.qa_panel_table_2018 TO hmda_user;

GRANT SELECT ON TABLE hmda_user.qa_panel_table_2018 TO hmda_user_rd;