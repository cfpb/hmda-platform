-- TODO: add 2020
-- for filers_using_exemption_by_agency
CREATE materialized VIEW hmda_user.exemptions_2018 tablespace pg_default
AS
SELECT
    ts.agency,
    COUNT(DISTINCT lar.lei)
FROM
    transmittalsheet2018 ts
    JOIN loanapplicationregister2018 lar ON lar.lei::text = ts.lei::text
WHERE
    length(lar.uli::text) < 23
    OR lower(lar.street::text) = 'exempt'::text
    OR lower(lar.city::text) = 'exempt'::text
    OR lower(lar.zip::text) = 'exempt'::text
    OR lower(lar.rate_spread::text) = 'exempt'::text
    OR lar.credit_score_applicant = 1111
    OR lar.credit_score_co_applicant = 1111
    OR lar.credit_score_type_applicant = 1111
    OR lar.credit_score_type_co_applicant = 1111
    OR lar.denial_reason1::text = '1111'::text
    OR lower(lar.total_loan_costs::text) = 'exempt'::text
    OR lower(lar.total_points::text) = 'exempt'::text
    OR lower(lar.origination_charges::text) = 'exempt'::text
    OR lower(lar.discount_points::text) = 'exempt'::text
    OR lower(lar.lender_credits::text) = 'exempt'::text
    OR lower(lar.interest_rate::text) = 'exempt'::text
    OR lower(lar.payment_penalty::text) = 'exempt'::text
    OR lower(lar.debt_to_incode::text) = 'exempt'::text
    OR lower(lar.loan_value_ratio::text) = 'exempt'::text
    OR lower(lar.loan_term::text) = 'exempt'::text
    OR lar.rate_spread_intro::text = '1111'::text
    OR lar.baloon_payment = 1111
    OR lar.insert_only_payment = 1111
    OR lar.amortization = 1111
    OR lar.other_amortization = 1111
    OR lower(lar.property_value::text) = 'exempt'::text
    OR lar.application_submission = 1111
    OR lar.lan_property_interest = 1111
    OR lower(lar.mf_affordable::text) = 'exempt'::text
    OR lar.home_security_policy = 1111
    OR lar.payable = 1111
    OR lower(lar.nmls::text) = 'exempt'::text
    OR lar.aus1_result = 1111
    OR lar.other_aus::text = '1111'::text
    OR lar.other_aus_result::text = '1111'::text
    OR lar.reverse_mortgage = 1111
    OR lar.line_of_credits = 1111
    OR lar.business_or_commercial = 1111
group by
    ts.agency WITH data;

CREATE materialized VIEW hmda_user.exemptions_2019 tablespace pg_default
AS
SELECT
    ts.agency,
    COUNT(DISTINCT lar.lei)
FROM
    transmittalsheet2019 ts
    JOIN loanapplicationregister2019 lar ON lar.lei::text = ts.lei::text
WHERE
    length(lar.uli::text) < 23
    OR lower(lar.street::text) = 'exempt'::text
    OR lower(lar.city::text) = 'exempt'::text
    OR lower(lar.zip::text) = 'exempt'::text
    OR lower(lar.rate_spread::text) = 'exempt'::text
    OR lar.credit_score_applicant = 1111
    OR lar.credit_score_co_applicant = 1111
    OR lar.credit_score_type_applicant = 1111
    OR lar.credit_score_type_co_applicant = 1111
    OR lar.denial_reason1::text = '1111'::text
    OR lower(lar.total_loan_costs::text) = 'exempt'::text
    OR lower(lar.total_points::text) = 'exempt'::text
    OR lower(lar.origination_charges::text) = 'exempt'::text
    OR lower(lar.discount_points::text) = 'exempt'::text
    OR lower(lar.lender_credits::text) = 'exempt'::text
    OR lower(lar.interest_rate::text) = 'exempt'::text
    OR lower(lar.payment_penalty::text) = 'exempt'::text
    OR lower(lar.debt_to_incode::text) = 'exempt'::text
    OR lower(lar.loan_value_ratio::text) = 'exempt'::text
    OR lower(lar.loan_term::text) = 'exempt'::text
    OR lar.rate_spread_intro::text = '1111'::text
    OR lar.baloon_payment = 1111
    OR lar.insert_only_payment = 1111
    OR lar.amortization = 1111
    OR lar.other_amortization = 1111
    OR lower(lar.property_value::text) = 'exempt'::text
    OR lar.application_submission = 1111
    OR lar.lan_property_interest = 1111
    OR lower(lar.mf_affordable::text) = 'exempt'::text
    OR lar.home_security_policy = 1111
    OR lar.payable = 1111
    OR lower(lar.nmls::text) = 'exempt'::text
    OR lar.aus1_result = 1111
    OR lar.other_aus::text = '1111'::text
    OR lar.other_aus_result::text = '1111'::text
    OR lar.reverse_mortgage = 1111
    OR lar.line_of_credits = 1111
    OR lar.business_or_commercial = 1111
group by
    ts.agency WITH data;

CREATE materialized VIEW hmda_user.exemptions_2020 tablespace pg_default
AS
SELECT
    ts.agency,
    COUNT(DISTINCT lar.lei)
FROM
    transmittalsheet2020 ts
    JOIN loanapplicationregister2020 lar ON lar.lei::text = ts.lei::text
WHERE
    length(lar.uli::text) < 23
    OR lower(lar.street::text) = 'exempt'::text
    OR lower(lar.city::text) = 'exempt'::text
    OR lower(lar.zip::text) = 'exempt'::text
    OR lower(lar.rate_spread::text) = 'exempt'::text
    OR lar.credit_score_applicant = 1111
    OR lar.credit_score_co_applicant = 1111
    OR lar.credit_score_type_applicant = 1111
    OR lar.credit_score_type_co_applicant = 1111
    OR lar.denial_reason1::text = '1111'::text
    OR lower(lar.total_loan_costs::text) = 'exempt'::text
    OR lower(lar.total_points::text) = 'exempt'::text
    OR lower(lar.origination_charges::text) = 'exempt'::text
    OR lower(lar.discount_points::text) = 'exempt'::text
    OR lower(lar.lender_credits::text) = 'exempt'::text
    OR lower(lar.interest_rate::text) = 'exempt'::text
    OR lower(lar.payment_penalty::text) = 'exempt'::text
    OR lower(lar.debt_to_incode::text) = 'exempt'::text
    OR lower(lar.loan_value_ratio::text) = 'exempt'::text
    OR lower(lar.loan_term::text) = 'exempt'::text
    OR lar.rate_spread_intro::text = '1111'::text
    OR lar.baloon_payment = 1111
    OR lar.insert_only_payment = 1111
    OR lar.amortization = 1111
    OR lar.other_amortization = 1111
    OR lower(lar.property_value::text) = 'exempt'::text
    OR lar.application_submission = 1111
    OR lar.lan_property_interest = 1111
    OR lower(lar.mf_affordable::text) = 'exempt'::text
    OR lar.home_security_policy = 1111
    OR lar.payable = 1111
    OR lower(lar.nmls::text) = 'exempt'::text
    OR lar.aus1_result = 1111
    OR lar.other_aus::text = '1111'::text
    OR lar.other_aus_result::text = '1111'::text
    OR lar.reverse_mortgage = 1111
    OR lar.line_of_credits = 1111
    OR lar.business_or_commercial = 1111
group by
    ts.agency WITH data;

-- for open_end_credit_filers_by_agency
CREATE materialized VIEW hmda_user.open_end_credit_filers_by_agency_2018 tablespace pg_default
AS
SELECT
    agency,
    ts.lei
FROM
    transmittalsheet2018 AS ts
WHERE
    Upper(ts.lei)
    IN (
        SELECT
            DISTINCT (Upper(lar.lei))
        FROM
            loanapplicationregister2018 AS lar
        GROUP BY
            lar.lei
        HAVING
            Sum( CASE WHEN line_of_credits = 1 THEN
                    1
                ELSE
                    0
END) > 0
)
WITH data;

CREATE materialized VIEW hmda_user.open_end_credit_filers_by_agency_2019 tablespace pg_default
AS
SELECT
    agency,
    ts.lei
FROM
    transmittalsheet2019 AS ts
WHERE
    Upper(ts.lei)
    IN (
        SELECT
            DISTINCT (Upper(lar.lei))
        FROM
            loanapplicationregister2019 AS lar
        GROUP BY
            lar.lei
        HAVING
            Sum( CASE WHEN line_of_credits = 1 THEN
                    1
                ELSE
                    0
END) > 0
)
WITH data;

-- for lar_count_using_exemption_by_agency
CREATE materialized VIEW hmda_user.lar_count_using_exemption_by_agency_2018 tablespace pg_default
AS
SELECT
    ts.agency,
    count(*)
FROM
    transmittalsheet2018 ts
    JOIN loanapplicationregister2018 lar ON lar.lei::text = ts.lei::text
WHERE
    length(lar.uli::text) < 23
    OR lower(lar.street::text) = 'exempt'::text
    OR lower(lar.city::text) = 'exempt'::text
    OR lower(lar.zip::text) = 'exempt'::text
    OR lower(lar.rate_spread::text) = 'exempt'::text
    OR lar.credit_score_applicant = 1111
    OR lar.credit_score_co_applicant = 1111
    OR lar.credit_score_type_applicant = 1111
    OR lar.credit_score_type_co_applicant = 1111
    OR lar.denial_reason1::text = '1111'::text
    OR lower(lar.total_loan_costs::text) = 'exempt'::text
    OR lower(lar.total_points::text) = 'exempt'::text
    OR lower(lar.origination_charges::text) = 'exempt'::text
    OR lower(lar.discount_points::text) = 'exempt'::text
    OR lower(lar.lender_credits::text) = 'exempt'::text
    OR lower(lar.interest_rate::text) = 'exempt'::text
    OR lower(lar.payment_penalty::text) = 'exempt'::text
    OR lower(lar.debt_to_incode::text) = 'exempt'::text
    OR lower(lar.loan_value_ratio::text) = 'exempt'::text
    OR lower(lar.loan_term::text) = 'exempt'::text
    OR lar.rate_spread_intro::text = '1111'::text
    OR lar.baloon_payment = 1111
    OR lar.insert_only_payment = 1111
    OR lar.amortization = 1111
    OR lar.other_amortization = 1111
    OR lower(lar.property_value::text) = 'exempt'::text
    OR lar.application_submission = 1111
    OR lar.lan_property_interest = 1111
    OR lower(lar.mf_affordable::text) = 'exempt'::text
    OR lar.home_security_policy = 1111
    OR lar.payable = 1111
    OR lower(lar.nmls::text) = 'exempt'::text
    OR lar.aus1_result = 1111
    OR lar.other_aus::text = '1111'::text
    OR lar.other_aus_result::text = '1111'::text
    OR lar.reverse_mortgage = 1111
    OR lar.line_of_credits = 1111
    OR lar.business_or_commercial = 1111
group by
    ts.agency WITH data;

CREATE materialized VIEW hmda_user.lar_count_using_exemption_by_agency_2019 tablespace pg_default
AS
SELECT
    ts.agency,
    count(*)
FROM
    transmittalsheet2019 ts
    JOIN loanapplicationregister2019 lar ON lar.lei::text = ts.lei::text
WHERE
    length(lar.uli::text) < 23
    OR lower(lar.street::text) = 'exempt'::text
    OR lower(lar.city::text) = 'exempt'::text
    OR lower(lar.zip::text) = 'exempt'::text
    OR lower(lar.rate_spread::text) = 'exempt'::text
    OR lar.credit_score_applicant = 1111
    OR lar.credit_score_co_applicant = 1111
    OR lar.credit_score_type_applicant = 1111
    OR lar.credit_score_type_co_applicant = 1111
    OR lar.denial_reason1::text = '1111'::text
    OR lower(lar.total_loan_costs::text) = 'exempt'::text
    OR lower(lar.total_points::text) = 'exempt'::text
    OR lower(lar.origination_charges::text) = 'exempt'::text
    OR lower(lar.discount_points::text) = 'exempt'::text
    OR lower(lar.lender_credits::text) = 'exempt'::text
    OR lower(lar.interest_rate::text) = 'exempt'::text
    OR lower(lar.payment_penalty::text) = 'exempt'::text
    OR lower(lar.debt_to_incode::text) = 'exempt'::text
    OR lower(lar.loan_value_ratio::text) = 'exempt'::text
    OR lower(lar.loan_term::text) = 'exempt'::text
    OR lar.rate_spread_intro::text = '1111'::text
    OR lar.baloon_payment = 1111
    OR lar.insert_only_payment = 1111
    OR lar.amortization = 1111
    OR lar.other_amortization = 1111
    OR lower(lar.property_value::text) = 'exempt'::text
    OR lar.application_submission = 1111
    OR lar.lan_property_interest = 1111
    OR lower(lar.mf_affordable::text) = 'exempt'::text
    OR lar.home_security_policy = 1111
    OR lar.payable = 1111
    OR lower(lar.nmls::text) = 'exempt'::text
    OR lar.aus1_result = 1111
    OR lar.other_aus::text = '1111'::text
    OR lar.other_aus_result::text = '1111'::text
    OR lar.reverse_mortgage = 1111
    OR lar.line_of_credits = 1111
    OR lar.business_or_commercial = 1111
group by
    ts.agency WITH data;

CREATE materialized VIEW hmda_user.lar_count_using_exemption_by_agency_2020 tablespace pg_default
AS
SELECT
    ts.agency,
    count(*)
FROM
    transmittalsheet2020 ts
    JOIN loanapplicationregister2020 lar ON lar.lei::text = ts.lei::text
WHERE
    length(lar.uli::text) < 23
    OR lower(lar.street::text) = 'exempt'::text
    OR lower(lar.city::text) = 'exempt'::text
    OR lower(lar.zip::text) = 'exempt'::text
    OR lower(lar.rate_spread::text) = 'exempt'::text
    OR lar.credit_score_applicant = 1111
    OR lar.credit_score_co_applicant = 1111
    OR lar.credit_score_type_applicant = 1111
    OR lar.credit_score_type_co_applicant = 1111
    OR lar.denial_reason1::text = '1111'::text
    OR lower(lar.total_loan_costs::text) = 'exempt'::text
    OR lower(lar.total_points::text) = 'exempt'::text
    OR lower(lar.origination_charges::text) = 'exempt'::text
    OR lower(lar.discount_points::text) = 'exempt'::text
    OR lower(lar.lender_credits::text) = 'exempt'::text
    OR lower(lar.interest_rate::text) = 'exempt'::text
    OR lower(lar.payment_penalty::text) = 'exempt'::text
    OR lower(lar.debt_to_incode::text) = 'exempt'::text
    OR lower(lar.loan_value_ratio::text) = 'exempt'::text
    OR lower(lar.loan_term::text) = 'exempt'::text
    OR lar.rate_spread_intro::text = '1111'::text
    OR lar.baloon_payment = 1111
    OR lar.insert_only_payment = 1111
    OR lar.amortization = 1111
    OR lar.other_amortization = 1111
    OR lower(lar.property_value::text) = 'exempt'::text
    OR lar.application_submission = 1111
    OR lar.lan_property_interest = 1111
    OR lower(lar.mf_affordable::text) = 'exempt'::text
    OR lar.home_security_policy = 1111
    OR lar.payable = 1111
    OR lower(lar.nmls::text) = 'exempt'::text
    OR lar.aus1_result = 1111
    OR lar.other_aus::text = '1111'::text
    OR lar.other_aus_result::text = '1111'::text
    OR lar.reverse_mortgage = 1111
    OR lar.line_of_credits = 1111
    OR lar.business_or_commercial = 1111
group by
    ts.agency WITH data;

-- for open_end_credit_lar_count_by_agency
CREATE materialized VIEW hmda_user.open_end_credit_lar_count_by_agency_2018 tablespace pg_default
AS
SELECT
    ts.agency,
    ts.lei
FROM
    transmittalsheet2018 AS ts
    LEFT JOIN loanapplicationregister2018 AS lar ON Upper(ts.lei) = Upper(lar.lei)
WHERE
    line_of_credits = 1 with data;

CREATE materialized VIEW hmda_user.open_end_credit_lar_count_by_agency_2019 tablespace pg_default
AS
SELECT
    ts.agency,
    ts.lei
FROM
    transmittalsheet2019 AS ts
    LEFT JOIN loanapplicationregister2019 AS lar ON Upper(ts.lei) = Upper(lar.lei)
WHERE
    line_of_credits = 1 with data;

-- for voluntary filers 
CREATE materialized VIEW hmda_user.voluntary_filers2018 tablespace pg_default 
AS (SELECT 'Less than 100 closed end originated loans', 
           Count(lei) AS lei_count, 
           SUM(count) AS total_lar 
    FROM   (SELECT lei, 
                   Count(*) 
            FROM   loanapplicationregister2018 
            WHERE  action_taken_type = '1' 
                   AND line_of_credits = '2' 
            GROUP  BY lei 
            HAVING Count(*) < 100) AS results) 
   UNION ALL 
   (SELECT 'Less than 200 open end originated loans', 
           Count(lei) AS lei_count, 
           SUM(count) AS total_lar 
    FROM   (SELECT lei, 
                   Count(*) 
            FROM   loanapplicationregister2018 
            WHERE  action_taken_type = '1' 
                   AND line_of_credits = '1' 
            GROUP  BY lei 
            HAVING Count(*) < 200) AS results2) 
   UNION ALL 
   (SELECT 'Less than 500 open end originated loans', 
           Count(lei) AS lei_count, 
           SUM(count) AS total_lar 
    FROM   (SELECT lei, 
                   Count(*) 
            FROM   loanapplicationregister2018 
            WHERE  action_taken_type = '1' 
                   AND line_of_credits = '1' 
            GROUP  BY lei 
            HAVING Count(*) < 500) AS results3) 
   UNION ALL 
   (SELECT 'Less than 100 originated loans - exempt', 
           Count(lei) AS lei_count, 
           SUM(count) AS total_lar 
    FROM   (SELECT lei, 
                   Count(*) 
            FROM   loanapplicationregister2018 
            WHERE  action_taken_type = '1' 
                   AND line_of_credits = '1111' 
            GROUP  BY lei 
            HAVING Count(*) < 200) AS results4); 
            
CREATE materialized VIEW hmda_user.voluntary_filers2019 tablespace pg_default 
AS (SELECT 'Less than 100 closed end originated loans', 
           Count(lei) AS lei_count, 
           SUM(count) AS total_lar 
    FROM   (SELECT lei, 
                   Count(*) 
            FROM   loanapplicationregister2019 
            WHERE  action_taken_type = '1' 
                   AND line_of_credits = '2' 
            GROUP  BY lei 
            HAVING Count(*) < 100) AS results) 
   UNION ALL 
   (SELECT 'Less than 200 open end originated loans', 
           Count(lei) AS lei_count, 
           SUM(count) AS total_lar 
    FROM   (SELECT lei, 
                   Count(*) 
            FROM   loanapplicationregister2019 
            WHERE  action_taken_type = '1' 
                   AND line_of_credits = '1' 
            GROUP  BY lei 
            HAVING Count(*) < 200) AS results2) 
   UNION ALL 
   (SELECT 'Less than 500 open end originated loans', 
           Count(lei) AS lei_count, 
           SUM(count) AS total_lar 
    FROM   (SELECT lei, 
                   Count(*) 
            FROM   loanapplicationregister2019 
            WHERE  action_taken_type = '1' 
                   AND line_of_credits = '1' 
            GROUP  BY lei 
            HAVING Count(*) < 500) AS results3) 
   UNION ALL 
   (SELECT 'Less than 100 originated loans - exempt', 
           Count(lei) AS lei_count, 
           SUM(count) AS total_lar 
    FROM   (SELECT lei, 
                   Count(*) 
            FROM   loanapplicationregister2019 
            WHERE  action_taken_type = '1' 
                   AND line_of_credits = '1111' 
            GROUP  BY lei 
            HAVING Count(*) < 200) AS results4);
            
           CREATE materialized VIEW hmda_user.voluntary_filers2020 tablespace pg_default 
AS (SELECT 'Less than 100 closed end originated loans', 
           Count(lei) AS lei_count, 
           SUM(count) AS total_lar 
    FROM   (SELECT lei, 
                   Count(*) 
            FROM   loanapplicationregister2020 
            WHERE  action_taken_type = '1' 
                   AND line_of_credits = '2' 
            GROUP  BY lei 
            HAVING Count(*) < 100) AS results) 
   UNION ALL 
   (SELECT 'Less than 200 open end originated loans', 
           Count(lei) AS lei_count, 
           SUM(count) AS total_lar 
    FROM   (SELECT lei, 
                   Count(*) 
            FROM   loanapplicationregister2020 
            WHERE  action_taken_type = '1' 
                   AND line_of_credits = '1' 
            GROUP  BY lei 
            HAVING Count(*) < 200) AS results2) 
   UNION ALL 
   (SELECT 'Less than 500 open end originated loans', 
           Count(lei) AS lei_count, 
           SUM(count) AS total_lar 
    FROM   (SELECT lei, 
                   Count(*) 
            FROM   loanapplicationregister2020 
            WHERE  action_taken_type = '1' 
                   AND line_of_credits = '1' 
            GROUP  BY lei 
            HAVING Count(*) < 500) AS results3) 
   UNION ALL 
   (SELECT 'Less than 100 originated loans - exempt', 
           Count(lei) AS lei_count, 
           SUM(count) AS total_lar 
    FROM   (SELECT lei, 
                   Count(*) 
            FROM   loanapplicationregister2020 
            WHERE  action_taken_type = '1' 
                   AND line_of_credits = '1111' 
            GROUP  BY lei 
            HAVING Count(*) < 200) AS results4);