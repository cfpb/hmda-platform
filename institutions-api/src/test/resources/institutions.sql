CREATE DATABASE hmda
  WITH OWNER = postgres
       ENCODING = 'UTF8'
       TABLESPACE = pg_default
       LC_COLLATE = 'en_US.utf8'
       LC_CTYPE = 'en_US.utf8'
       CONNECTION LIMIT = -1;

CREATE TABLE public.institutions2018
(
  lei character varying NOT NULL,
  activity_year integer NOT NULL,
  agency integer NOT NULL,
  institution_type integer NOT NULL,
  id2017 character varying NOT NULL,
  tax_id character varying NOT NULL,
  rssd integer NOT NULL,
  respondent_name character varying NOT NULL,
  respondent_state character varying NOT NULL,
  respondent_city character varying NOT NULL,
  parent_id_rssd integer NOT NULL,
  parent_name character varying NOT NULL,
  assets integer NOT NULL,
  other_lender_code integer NOT NULL,
  topholder_id_rssd integer NOT NULL,
  topholder_name character varying NOT NULL,
  hmda_filer boolean NOT NULL,
  CONSTRAINT institutions2018_pkey PRIMARY KEY (lei)
)
WITH (
  OIDS=FALSE
);
ALTER TABLE public.institutions2018
  OWNER TO postgres;

CREATE SEQUENCE institutions_emails_2018_id_seq START 1;

CREATE TABLE public.institutions_emails_2018
(
  id integer NOT NULL DEFAULT nextval('institutions_emails_2018_id_seq'::regclass),
  lei character varying NOT NULL REFERENCES institutions2018 (lei),
  email_domain character varying NOT NULL,
  CONSTRAINT institutions_emails_2018_pkey PRIMARY KEY (id )
)
WITH (
  OIDS=FALSE
);
ALTER TABLE public.institutions_emails_2018
  OWNER TO postgres;


INSERT INTO institutions2018 VALUES(
  '54930084UKLVMY22DS16',
  2019,
  1,
  17,
  '12345',
  '99-00000000',
  12345,
  'xvavjuitZa',
  'NC',
  'Raleigh',
  1520162208,
  'Parent Name',
  450,
  1406639146,
  442825905,
  'TopHolder Name',
   true);


INSERT INTO institutions2018 VALUES(
  '74930084UKLVMY22DS16',
  2019,
  1,
  17,
  '12345',
  '99-00000000',
  12345,
  'xvavjuitZa',
  'NC',
  'Raleigh',
  1520162208,
  'Parent Name',
  450,
  1406639146,
  442825905,
  'TopHolder Name',
   true);

INSERT INTO institutions_emails_2018 VALUES(
  1,
  '54930084UKLVMY22DS16',
  'aaa.com'
);

INSERT INTO institutions_emails_2018 VALUES(
  2,
  '54930084UKLVMY22DS16',
  'bbb.com'
);

INSERT INTO institutions_emails_2018 VALUES(
  3,
  '74930084UKLVMY22DS16',
  'bbb.com'
);