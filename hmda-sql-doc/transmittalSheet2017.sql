ALTER TABLE modifiedlar2017_ultimate ADD COLUMN filing_year varchar;
ALTER TABLE modifiedlar2017_ultimate ALTER COLUMN filing_year SET default "2017"
ALTER TABLE modifiedlar2017_ultimate ADD COLUMN arid varchar;
UPDATE modifiedlar2017_ultimate SET countyCombined=CONCAT(agency,respondent_id);
ALTER TABLE modifiedlar2017_ultimate ADD COLUMN countyCombined varchar;
UPDATE modifiedlar2017_ultimate SET countyCombined=CONCAT(state,county);
CREATE index ON modifiedlar2017_ultimate (countyCombined);
CREATE index ON modifiedlar2017_ultimate (msa_md);
CREATE index ON modifiedlar2017_ultimate (state);
CREATE index ON modifiedlar2017_ultimate (arid);
CREATE index ON modifiedlar2017_ultimate (action_taken_type);
CREATE index ON modifiedlar2017_ultimate (lien_status);
CREATE index ON modifiedlar2017_ultimate (loan_purpose);
CREATE index ON modifiedlar2017_ultimate (property_type);
