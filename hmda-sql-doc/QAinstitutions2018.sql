-- Table: hmda_user.qa_panel_table_2018

-- DROP TABLE hmda_user.qa_panel_table_2018;

CREATE TABLE hmda_user.qa_panel_table_2018
(
    lei character varying COLLATE pg_catalog."default" NOT NULL,
    activity_year integer NOT NULL,
    agency integer NOT NULL,
    institution_type integer NOT NULL,
    id2017 character varying COLLATE pg_catalog."default" NOT NULL,
    tax_id character varying COLLATE pg_catalog."default" NOT NULL,
    rssd integer NOT NULL,
    respondent_name character varying COLLATE pg_catalog."default" NOT NULL,
    respondent_state character varying COLLATE pg_catalog."default" NOT NULL,
    respondent_city character varying COLLATE pg_catalog."default" NOT NULL,
    parent_id_rssd integer NOT NULL,
    parent_name character varying COLLATE pg_catalog."default" NOT NULL,
    assets integer NOT NULL,
    other_lender_code integer NOT NULL,
    topholder_id_rssd integer NOT NULL,
    topholder_name character varying COLLATE pg_catalog."default" NOT NULL,
    hmda_filer boolean NOT NULL,
    quarterly_filer boolean NOT NULL,
    quarterly_filer_has_filed_q1 boolean NOT NULL,
    quarterly_filer_has_filed_q2 boolean NOT NULL,
    quarterly_filer_has_filed_q3 boolean NOT NULL,
    notes character varying COLLATE pg_catalog."default"
 )
WITH (
    OIDS = FALSE
)
TABLESPACE pg_default;

ALTER TABLE hmda_user.qa_panel_table_2018
    OWNER to hmda_user;

GRANT ALL ON TABLE hmda_user.qa_panel_table_2018 TO hmda_user;

GRANT SELECT ON TABLE hmda_user.qa_panel_table_2018 TO hmda_user_rd;