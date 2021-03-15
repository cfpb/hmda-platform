-- Table: hmda_user.ts2021_q1

-- DROP TABLE hmda_user.ts2021_q1;

CREATE TABLE hmda_user.ts2021_q1
(
    id integer,
    institution_name character varying COLLATE pg_catalog."default",
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
    lei character varying COLLATE pg_catalog."default",
    created_at timestamp without time zone,
    submission_id character varying COLLATE pg_catalog."default",
    is_quarterly boolean,
    sign_date bigint
)
WITH (
    OIDS = FALSE
)
TABLESPACE pg_default;

ALTER TABLE hmda_user.ts2021_q1
    OWNER to hmda_user;

GRANT ALL ON TABLE hmda_user.ts2021_q1 TO hmda_user;

GRANT SELECT ON TABLE hmda_user.ts2021_q1 TO hmda_user_rd;