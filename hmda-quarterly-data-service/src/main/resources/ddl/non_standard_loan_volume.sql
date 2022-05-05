create materialized view non_standard_loan_volume
as
	select now() last_updated, baloon_payment, reverse_mortgage, business_or_commercial, insert_only_payment, amortization, count(*) agg,
	date_part('year', to_timestamp(action_taken_date::varchar(8), 'yyyymmdd')) || '-Q' || date_part('quarter', to_timestamp(action_taken_date::varchar(8), 'yyyymmdd')) quarter
	from loanapplicationregister2018_snapshot
	where action_taken_type = 1
		and (baloon_payment = 1
			or reverse_mortgage = 1
			or business_or_commercial = 1
			or insert_only_payment = 1
			or amortization = 1
		)
	group by baloon_payment, reverse_mortgage, business_or_commercial, insert_only_payment, amortization, quarter
	union
	select now() last_updated, baloon_payment, reverse_mortgage, business_or_commercial, insert_only_payment, amortization, count(*) agg,
	date_part('year', to_timestamp(action_taken_date::varchar(8), 'yyyymmdd')) || '-Q' || date_part('quarter', to_timestamp(action_taken_date::varchar(8), 'yyyymmdd')) quarter
	from loanapplicationregister2019_snapshot
	where action_taken_type = 1
		and (baloon_payment = 1
			or reverse_mortgage = 1
			or business_or_commercial = 1
			or insert_only_payment = 1
			or amortization = 1
		)
	group by baloon_payment, reverse_mortgage, business_or_commercial, insert_only_payment, amortization, quarter
	union
	select now() last_updated, baloon_payment, reverse_mortgage, business_or_commercial, insert_only_payment, amortization, count(*) agg,
	date_part('year', to_timestamp(action_taken_date::varchar(8), 'yyyymmdd')) || '-Q' || date_part('quarter', to_timestamp(action_taken_date::varchar(8), 'yyyymmdd')) quarter
	from loanapplicationregister2020_snapshot
	where action_taken_type = 1
		and (baloon_payment = 1
			or reverse_mortgage = 1
			or business_or_commercial = 1
			or insert_only_payment = 1
			or amortization = 1
		)
	group by baloon_payment, reverse_mortgage, business_or_commercial, insert_only_payment, amortization, quarter
with data;