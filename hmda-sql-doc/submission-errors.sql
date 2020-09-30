CREATE TABLE submission_errors(
    lei               VARCHAR,
    period            VARCHAR,
    sequence_number   INT,
    submission_status INT,
    edit_name         VARCHAR,
    loan_data         TEXT ARRAY,
    created_date      TIMESTAMP,
    updated_date      TIMESTAMP,
    CONSTRAINT submission_errors_pkey PRIMARY KEY (lei, period, sequence_number, edit_name)
);