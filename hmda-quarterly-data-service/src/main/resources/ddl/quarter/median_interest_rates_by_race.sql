DROP MATERIALIZED VIEW  IF EXISTS median_interest_rates_by_race_{mv_suffix};

create materialized view median_interest_rates_by_race_{mv_suffix} as
  select 
    now() as last_updated, 
    case 
      when line_of_credits = 1 
      then 1 
      else loan_type 
    end as lt,
    case 
      when line_of_credits = 1 or loan_type != 1 
      then '' 
      else conforming_loan_limit 
    end as cll,
    line_of_credits as loc,
    percentile_cont(0.5) within group(order by interest_rate ::numeric) 
    as median_interest_rate,
    derive_race_ethnicity(
      race_applicant_1, race_applicant_2, race_applicant_3, race_applicant_4, race_applicant_5,
      race_co_applicant_1, race_co_applicant_2, race_co_applicant_3, race_co_applicant_4, race_co_applicant_5,
      ethnicity_applicant_1, ethnicity_applicant_2, ethnicity_applicant_3, ethnicity_applicant_4, ethnicity_applicant_5,
      ethnicity_co_applicant_1, ethnicity_co_applicant_2, ethnicity_co_applicant_3, ethnicity_co_applicant_4, ethnicity_co_applicant_5
    ) as race_ethnicity,
    date_part('year', to_timestamp(action_taken_date::varchar(8), 'yyyymmdd')) || '-Q' || date_part('quarter', to_timestamp(action_taken_date::varchar(8), 'yyyymmdd')) as quarter
  from {lar_table_name}
  where action_taken_type = 1
    and occupancy_type = 1
    and total_uits in (1, 2, 3, 4)
    and construction_method = '1'
    and business_or_commercial = 2
    and reverse_mortgage = 2
    and insert_only_payment = 2
    and amortization = 2
    and baloon_payment = 2
    and ((line_of_credits = 1 and lien_status in (1,2)) or (line_of_credits = 2 and lien_status  = 1) ) -- heloc or everything else
    and interest_rate ~ '^[0-9\.]+$'
    and lei in (select lei from {inst_table_name} where quarterly_filer = true)
    {additional_clause}
    {filter_loan_purpose}
  group by quarter, race_ethnicity, lt, loc, cll
  having count(*) >= 100 -- cell suppression

with data;