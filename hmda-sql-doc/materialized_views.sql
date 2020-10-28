-- TODO: add 2020

-- for filers_using_exemption_by_agency
CREATE materialized VIEW hmda_user.exemptions_2018 tablespace pg_default AS
SELECT ts.agency,
       lar.lei
FROM   transmittalsheet2018 ts
JOIN   loanapplicationregister2018 lar
ON     lar.lei::text = ts.lei::text
WHERE  length(lar.uli::text) < 23
OR     lower(lar.street::text) = 'exempt'::text
OR     lower(lar.city::text) = 'exempt'::text
OR     lower(lar.zip::text) = 'exempt'::text
OR     lower(lar.rate_spread::text) = 'exempt'::text
OR     lar.credit_score_applicant = 1111
OR     lar.credit_score_co_applicant = 1111
OR     lar.credit_score_type_applicant = 1111
OR     lar.credit_score_type_co_applicant = 1111
OR     lar.denial_reason1::text = '1111'::text
OR     lower(lar.total_loan_costs::text) = 'exempt'::text
OR     lower(lar.total_points::text) = 'exempt'::text
OR     lower(lar.origination_charges::text) = 'exempt'::text
OR     lower(lar.discount_points::text) = 'exempt'::text
OR     lower(lar.lender_credits::text) = 'exempt'::text
OR     lower(lar.interest_rate::text) = 'exempt'::text
OR     lower(lar.payment_penalty::text) = 'exempt'::text
OR     lower(lar.debt_to_incode::text) = 'exempt'::text
OR     lower(lar.loan_value_ratio::text) = 'exempt'::text
OR     lower(lar.loan_term::text) = 'exempt'::text
OR     lar.rate_spread_intro::text = '1111'::text
OR     lar.baloon_payment = 1111
OR     lar.insert_only_payment = 1111
OR     lar.amortization = 1111
OR     lar.other_amortization = 1111
OR     lower(lar.property_value::text) = 'exempt'::text
OR     lar.application_submission = 1111
OR     lar.lan_property_interest = 1111
OR     lower(lar.mf_affordable::text) = 'exempt'::text
OR     lar.home_security_policy = 1111
OR     lar.payable = 1111
OR     lower(lar.nmls::text) = 'exempt'::text
OR     lar.aus1_result = 1111
OR     lar.other_aus::text = '1111'::text
OR     lar.other_aus_result::text = '1111'::text
OR     lar.reverse_mortgage = 1111
OR     lar.line_of_credits = 1111
OR     lar.business_or_commercial = 1111
WITH data;

CREATE materialized VIEW hmda_user.exemptions_2019 tablespace pg_default AS
SELECT ts.agency,
       lar.lei
FROM   transmittalsheet2019 ts
JOIN   loanapplicationregister2019 lar
ON     lar.lei::text = ts.lei::text
WHERE  length(lar.uli::text) < 23
OR     lower(lar.street::text) = 'exempt'::text
OR     lower(lar.city::text) = 'exempt'::text
OR     lower(lar.zip::text) = 'exempt'::text
OR     lower(lar.rate_spread::text) = 'exempt'::text
OR     lar.credit_score_applicant = 1111
OR     lar.credit_score_co_applicant = 1111
OR     lar.credit_score_type_applicant = 1111
OR     lar.credit_score_type_co_applicant = 1111
OR     lar.denial_reason1::text = '1111'::text
OR     lower(lar.total_loan_costs::text) = 'exempt'::text
OR     lower(lar.total_points::text) = 'exempt'::text
OR     lower(lar.origination_charges::text) = 'exempt'::text
OR     lower(lar.discount_points::text) = 'exempt'::text
OR     lower(lar.lender_credits::text) = 'exempt'::text
OR     lower(lar.interest_rate::text) = 'exempt'::text
OR     lower(lar.payment_penalty::text) = 'exempt'::text
OR     lower(lar.debt_to_incode::text) = 'exempt'::text
OR     lower(lar.loan_value_ratio::text) = 'exempt'::text
OR     lower(lar.loan_term::text) = 'exempt'::text
OR     lar.rate_spread_intro::text = '1111'::text
OR     lar.baloon_payment = 1111
OR     lar.insert_only_payment = 1111
OR     lar.amortization = 1111
OR     lar.other_amortization = 1111
OR     lower(lar.property_value::text) = 'exempt'::text
OR     lar.application_submission = 1111
OR     lar.lan_property_interest = 1111
OR     lower(lar.mf_affordable::text) = 'exempt'::text
OR     lar.home_security_policy = 1111
OR     lar.payable = 1111
OR     lower(lar.nmls::text) = 'exempt'::text
OR     lar.aus1_result = 1111
OR     lar.other_aus::text = '1111'::text
OR     lar.other_aus_result::text = '1111'::text
OR     lar.reverse_mortgage = 1111
OR     lar.line_of_credits = 1111
OR     lar.business_or_commercial = 1111
WITH data;

-- for open_end_credit_filers_by_agency
CREATE materialized VIEW hmda_user.open_end_credit_filers_by_agency_2018 tablespace pg_default AS
SELECT agency,
       ts.lei
FROM   transmittalsheet2018 AS ts
WHERE  Upper(ts.lei) IN
     (
     SELECT DISTINCT( Upper(lar.lei) )
     FROM            loanapplicationregister2018 AS lar
     GROUP BY        lar.lei
     HAVING          Sum(
                     CASE
                             WHEN line_of_credits = 1 THEN 1
                             ELSE 0
                     END) > 0)
WITH data;

CREATE materialized VIEW hmda_user.open_end_credit_filers_by_agency_2019 tablespace pg_default AS
SELECT agency,
       ts.lei
FROM   transmittalsheet2019 AS ts
WHERE  Upper(ts.lei) IN
     (
     SELECT DISTINCT( Upper(lar.lei) )
     FROM            loanapplicationregister2019 AS lar
     GROUP BY        lar.lei
     HAVING          Sum(
                     CASE
                             WHEN line_of_credits = 1 THEN 1
                             ELSE 0
                     END) > 0)
WITH data;

-- for lar_count_using_exemption_by_agency
CREATE materialized VIEW hmda_user.lar_count_using_exemption_by_agency_2018 tablespace pg_default AS
SELECT 	lar.lei, agency
FROM      loanapplicationregister2019 AS lar
LEFT JOIN transmittalsheet2019  AS ts
ON        upper(lar.lei) = upper(ts.lei)
WHERE     lar.street = 'exempt'
OR        lar.city = 'exempt'
OR        lar.zip = 'exempt'
OR        rate_spread = 'exempt'
OR        credit_score_applicant = '1111'
OR        credit_score_co_applicant = '1111'
OR        credit_score_type_applicant = '1111'
OR        credit_score_type_co_applicant = '1111'
OR        denial_reason1 = '1111'
OR        total_loan_costs = 'exempt'
OR        total_points = 'exempt'
OR        origination_charges = 'exempt'
OR        discount_points = 'exempt'
OR        lender_credits = 'exempt'
OR        interest_rate = 'exempt'
OR        payment_penalty = 'exempt'
OR        debt_to_incode = 'exempt'
OR        loan_value_ratio = 'exempt'
OR        loan_term = 'exempt'
OR        rate_spread_intro = '1111'
OR        baloon_payment = '1111'
OR        insert_only_payment = '1111'
OR        amortization = '1111'
OR        other_amortization = '1111'
OR        property_value = 'exempt'
OR        application_submission = '1111'
OR        lan_property_interest = '1111'
OR        mf_affordable = 'exempt'
OR        home_security_policy = '1111'
OR        payable = '1111'
OR        nmls = 'exempt'
OR        aus1_result = '1111'
OR        other_aus = '1111'
OR        other_aus_result = '1111'
OR        reverse_mortgage = '1111'
OR        line_of_credits = '1111'
OR        business_or_commercial = '1111';
WITH data;

CREATE materialized VIEW hmda_user.lar_count_using_exemption_by_agency_2019 tablespace pg_default AS
SELECT 	lar.lei, agency
FROM      loanapplicationregister2019 AS lar
LEFT JOIN transmittalsheet2019  AS ts
ON        upper(lar.lei) = upper(ts.lei)
WHERE     lar.street = 'exempt'
OR        lar.city = 'exempt'
OR        lar.zip = 'exempt'
OR        rate_spread = 'exempt'
OR        credit_score_applicant = '1111'
OR        credit_score_co_applicant = '1111'
OR        credit_score_type_applicant = '1111'
OR        credit_score_type_co_applicant = '1111'
OR        denial_reason1 = '1111'
OR        total_loan_costs = 'exempt'
OR        total_points = 'exempt'
OR        origination_charges = 'exempt'
OR        discount_points = 'exempt'
OR        lender_credits = 'exempt'
OR        interest_rate = 'exempt'
OR        payment_penalty = 'exempt'
OR        debt_to_incode = 'exempt'
OR        loan_value_ratio = 'exempt'
OR        loan_term = 'exempt'
OR        rate_spread_intro = '1111'
OR        baloon_payment = '1111'
OR        insert_only_payment = '1111'
OR        amortization = '1111'
OR        other_amortization = '1111'
OR        property_value = 'exempt'
OR        application_submission = '1111'
OR        lan_property_interest = '1111'
OR        mf_affordable = 'exempt'
OR        home_security_policy = '1111'
OR        payable = '1111'
OR        nmls = 'exempt'
OR        aus1_result = '1111'
OR        other_aus = '1111'
OR        other_aus_result = '1111'
OR        reverse_mortgage = '1111'
OR        line_of_credits = '1111'
OR        business_or_commercial = '1111';
WITH data;

-- for open_end_credit_lar_count_by_agency
CREATE materialized VIEW hmda_user.open_end_credit_lar_count_by_agency_2018 tablespace pg_default AS
SELECT    ts.agency,
          ts.lei
FROM      transmittalsheet2018        AS ts
LEFT JOIN loanapplicationregister2018 AS lar
ON        Upper(ts.lei) = Upper(lar.lei)
WHERE     line_of_credits = 1 with data;

CREATE materialized VIEW hmda_user.open_end_credit_lar_count_by_agency_2019 tablespace pg_default AS
SELECT    ts.agency,
          ts.lei
FROM      transmittalsheet2019        AS ts
LEFT JOIN loanapplicationregister2019 AS lar
ON        Upper(ts.lei) = Upper(lar.lei)
WHERE     line_of_credits = 1 with data;