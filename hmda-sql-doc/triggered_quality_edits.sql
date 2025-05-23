CREATE TABLE triggered_quality_edits(
    lei               VARCHAR,
    period            VARCHAR,
    sequence_number   INT,
    submission_status INT,
    edit_name         VARCHAR,
    loan_data         TEXT ARRAY,
    submission_start_date    TIMESTAMP,
    submission_end_date      TIMESTAMP,
    fields            JSONB,
    created_date      TIMESTAMP,
    updated_date      TIMESTAMP,
    CONSTRAINT triggered_quality_edits_pkey PRIMARY KEY (lei, period, sequence_number, edit_name)
);