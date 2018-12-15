CREATE DATABASE hmda
  WITH OWNER = postgres
       ENCODING = 'UTF8'
       TABLESPACE = pg_default
       LC_COLLATE = 'en_US.utf8'
       LC_CTYPE = 'en_US.utf8'
       CONNECTION LIMIT = -1;

CREATE TABLE public.transmittalsheet2018 (
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
    CONSTRAINT ts2018_pkey PRIMARY KEY (lei)
)
WITH (
  OIDS=FALSE
);
ALTER TABLE public.transmittalsheet2018
  OWNER TO postgres;