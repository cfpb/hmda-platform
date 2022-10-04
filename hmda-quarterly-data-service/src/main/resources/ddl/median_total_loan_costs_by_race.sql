create materialized view median_total_loan_costs_by_race_2018 as
	select now() last_updated, percentile_cont(0.5) within group(order by total_loan_costs::numeric) median_total_loan_costs,
		case when line_of_credits = 1 then 1 else loan_type end lt,
		case when line_of_credits = 1 or loan_type != 1 then '' else conforming_loan_limit end cll,
		line_of_credits loc,
		derive_race_ethnicity(
			race_applicant_1, race_applicant_2, race_applicant_3, race_applicant_4, race_applicant_5,
			race_co_applicant_1, race_co_applicant_2, race_co_applicant_3, race_co_applicant_4, race_co_applicant_5,
			ethnicity_applicant_1, ethnicity_applicant_2, ethnicity_applicant_3, ethnicity_applicant_4, ethnicity_applicant_5,
			ethnicity_co_applicant_1, ethnicity_co_applicant_2, ethnicity_co_applicant_3, ethnicity_co_applicant_4, ethnicity_co_applicant_5
		) race_ethnicity,
		date_part('year', to_timestamp(action_taken_date::varchar(8), 'yyyymmdd')) || '-Q' || date_part('quarter', to_timestamp(action_taken_date::varchar(8), 'yyyymmdd')) quarter
	from loanapplicationregister2018_qpub_06082022
	where lien_status = 1
		and occupancy_type = 1
		and total_uits in (1, 2, 3, 4)
		and construction_method = '1'
		and business_or_commercial != 1
		and reverse_mortgage != 1
		and insert_only_payment != 1
		and amortization != 1
		and baloon_payment != 1
		and line_of_credits in (1, 2)
		and total_loan_costs ~ '^[0-9\.]+$'
		and lei in (select lei from institutions2022 where quarterly_filer = true)
--		and action_taken_date >= 20220401 (for quarterly, specify the quarter date range)
	group by quarter, race_ethnicity, lt, loc, cll
with data;