create materialized view median_credit_score_by_loan_type as
	select now() last_updated, loan_type, '' cll, case when loan_type = 1 then line_of_credits else 0 end loc, percentile_cont(0.5) within group(order by credit_score_applicant) median_credit_score,
	date_part('year', to_timestamp(action_taken_date::varchar(8), 'yyyymmdd')) || '-Q' || date_part('quarter', to_timestamp(action_taken_date::varchar(8), 'yyyymmdd')) quarter
	from loanapplicationregister2018_three_year_04052022
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
	group by quarter, loan_type, cll, loc
	union
	select now() last_updated, loan_type, case when loan_type = 1 then conforming_loan_limit else '' end cll, case when loan_type = 1 then line_of_credits else 0 end loc, percentile_cont(0.5) within group(order by credit_score_applicant) median_credit_score,
	date_part('year', to_timestamp(action_taken_date::varchar(8), 'yyyymmdd')) || '-Q' || date_part('quarter', to_timestamp(action_taken_date::varchar(8), 'yyyymmdd')) quarter
	from loanapplicationregister2019_one_year_04052022
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
	group by quarter, loan_type, cll, loc
	union
	select now() last_updated, loan_type, case when loan_type = 1 then conforming_loan_limit else '' end cll, case when loan_type = 1 then line_of_credits else 0 end loc, percentile_cont(0.5) within group(order by credit_score_applicant) median_credit_score,
	date_part('year', to_timestamp(action_taken_date::varchar(8), 'yyyymmdd')) || '-Q' || date_part('quarter', to_timestamp(action_taken_date::varchar(8), 'yyyymmdd')) quarter
	from loanapplicationregister2020_one_year_04302022
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
	group by quarter, loan_type, cll, loc
	union
	select now() last_updated, loan_type, case when loan_type = 1 then conforming_loan_limit else '' end cll, case when loan_type = 1 then line_of_credits else 0 end loc, percentile_cont(0.5) within group(order by credit_score_applicant) median_credit_score,
	date_part('year', to_timestamp(action_taken_date::varchar(8), 'yyyymmdd')) || '-Q' || date_part('quarter', to_timestamp(action_taken_date::varchar(8), 'yyyymmdd')) quarter
	from loanapplicationregister2021_snapshot_04302022
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
	group by quarter, loan_type, cll, loc
with data;