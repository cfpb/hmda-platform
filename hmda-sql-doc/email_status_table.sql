
CREATE TABLE hmda_email_status (
    lei VARCHAR NOT NULL,
    year INTEGER NOT NULL,
    submission_id VARCHAR NOT NULL PRIMARY KEY,
    email_address VARCHAR NOT NULL,
    successful BOOLEAN NOT NULL,
    failure_reason VARCHAR
);