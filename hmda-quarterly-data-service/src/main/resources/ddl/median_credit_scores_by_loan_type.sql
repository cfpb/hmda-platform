create materialized view median_credit_score_by_loan_type_2018 as
	select now() last_updated, case when line_of_credits = 1 then 1 else loan_type end lt,
		case when line_of_credits = 1 or loan_type != 1 then '' else conforming_loan_limit end cll,
		line_of_credits loc, percentile_cont(0.5) within group(order by credit_score_applicant) median_credit_score,
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
		and credit_score_applicant < 1111
		and lei in (select lei from institutions2022 where quarterly_filer = true)
	group by quarter, lt, cll, loc
with data;