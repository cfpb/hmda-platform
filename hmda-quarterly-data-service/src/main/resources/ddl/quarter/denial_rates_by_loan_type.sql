DROP MATERIALIZED VIEW  IF EXISTS denial_rates_{mv_suffix};

create materialized view denial_rates_{mv_suffix} as
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
    case
      when count(*) filter(where action_taken_type in (1,2,3)) <= 0
      then 0 
      else count(*) filter (where action_taken_type = 3)::numeric / count(*) filter(where action_taken_type in (1,2,3)) * 100
    end as denial_rate,
    date_part('year', to_timestamp(action_taken_date::varchar(8), 'yyyymmdd')) || '-Q' || date_part('quarter', to_timestamp(action_taken_date::varchar(8), 'yyyymmdd')) as quarter
  from {lar_table_name}
  where action_taken_type not in (6,7,8)
    and occupancy_type = 1
    and total_uits in (1, 2, 3, 4)
    and construction_method = '1'
    and business_or_commercial = 2
    and reverse_mortgage = 2
    and insert_only_payment = 2
    and amortization = 2
    and baloon_payment = 2
    and ((line_of_credits = 1 and lien_status in (1,2)) or (line_of_credits = 2 and lien_status  = 1) ) -- heloc or everything else
    and lei in (select lei from {inst_table_name} where quarterly_filer = true)
    {additional_clause}
    {filter_loan_purpose}
  group by quarter, lt, cll, loc
  having count(*) >= 100 -- cell suppression
with data;