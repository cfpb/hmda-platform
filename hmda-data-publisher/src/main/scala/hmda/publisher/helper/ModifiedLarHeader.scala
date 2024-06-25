package hmda.publisher.helper

trait ModifiedLarHeader {

  val MLARHeader = "activity_year|lei|derived_msa_md|state_code|county_code|census_tract|conforming_loan_limit|" +
    "derived_loan_product_type|derived_dwelling_category|derived_ethnicity|derived_race|derived_sex|" +
    "action_taken|purchaser_type|preapproval|loan_type|loan_purpose|lien_status|reverse_mortgage|" +
    "open_end_line_of_credit|business_or_commercial_purpose|loan_amount|combined_loan_to_value_ratio|interest_rate|" +
    "rate_spread|hoepa_status|total_loan_costs|total_points_and_fees|origination_charges|discount_points|" +
    "lender_credits|loan_term|prepayment_penalty_term|intro_rate_period|negative_amortization|" +
    "interest_only_payment|balloon_payment|other_nonamortizing_features|property_value|construction_method|" +
    "occupancy_type|manufactured_home_secured_property_type|manufactured_home_land_property_interest|" +
    "total_units|multifamily_affordable_units|income|debt_to_income_ratio|applicant_credit_score_type|" +
    "co_applicant_credit_score_type|applicant_ethnicity_1|applicant_ethnicity_2|applicant_ethnicity_3|" +
    "applicant_ethnicity_4|applicant_ethnicity_5|co_applicant_ethnicity_1|co_applicant_ethnicity_2|" +
    "co_applicant_ethnicity_3|co_applicant_ethnicity_4|co_applicant_ethnicity_5|applicant_ethnicity_observed|" +
    "co_applicant_ethnicity_observed|applicant_race_1|applicant_race_2|applicant_race_3|applicant_race_4|" +
    "applicant_race_5|co_applicant_race_1|co_applicant_race_2|co_applicant_race_3|co_applicant_race_4|" +
    "co_applicant_race_5|applicant_race_observed|co_applicant_race_observed|applicant_sex|co_applicant_sex|" +
    "applicant_sex_observed|co_applicant_sex_observed|applicant_age|co_applicant_age|applicant_age_above_62|" +
    "co_applicant_age_above_62|submission_of_application|initially_payable_to_institution|aus_1|aus_2|aus_3|" +
    "aus_4|aus_5|denial_reason_1|denial_reason_2|denial_reason_3|denial_reason_4|tract_population|" +
    "tract_minority_population_percent|ffiec_msa_md_median_family_income|tract_to_msa_income_percentage|" +
    "tract_owner_occupied_units|tract_one_to_four_family_homes|tract_median_age_of_housing_units" + "\n"


  val MLARHeaderCSV = "activity_year,lei,derived_msa_md,state_code,county_code,census_tract,conforming_loan_limit," +
    "derived_loan_product_type,derived_dwelling_category,derived_ethnicity,derived_race,derived_sex," +
    "action_taken,purchaser_type,preapproval,loan_type,loan_purpose,lien_status,reverse_mortgage," +
    "open_end_line_of_credit,business_or_commercial_purpose,loan_amount,combined_loan_to_value_ratio,interest_rate," +
    "rate_spread,hoepa_status,total_loan_costs,total_points_and_fees,origination_charges,discount_points," +
    "lender_credits,loan_term,prepayment_penalty_term,intro_rate_period,negative_amortization," +
    "interest_only_payment,balloon_payment,other_nonamortizing_features,property_value,construction_method," +
    "occupancy_type,manufactured_home_secured_property_type,manufactured_home_land_property_interest," +
    "total_units,multifamily_affordable_units,income,debt_to_income_ratio,applicant_credit_score_type," +
    "co_applicant_credit_score_type,applicant_ethnicity_1,applicant_ethnicity_2,applicant_ethnicity_3," +
    "applicant_ethnicity_4,applicant_ethnicity_5,co_applicant_ethnicity_1,co_applicant_ethnicity_2," +
    "co_applicant_ethnicity_3,co_applicant_ethnicity_4,co_applicant_ethnicity_5,applicant_ethnicity_observed," +
    "co_applicant_ethnicity_observed,applicant_race_1,applicant_race_2,applicant_race_3,applicant_race_4," +
    "applicant_race_5,co_applicant_race_1,co_applicant_race_2,co_applicant_race_3,co_applicant_race_4," +
    "co_applicant_race_5,applicant_race_observed,co_applicant_race_observed,applicant_sex,co_applicant_sex," +
    "applicant_sex_observed,co_applicant_sex_observed,applicant_age,co_applicant_age,applicant_age_above_62," +
    "co_applicant_age_above_62,submission_of_application,initially_payable_to_institution,aus_1,aus_2,aus_3," +
    "aus_4,aus_5,denial_reason_1,denial_reason_2,denial_reason_3,denial_reason_4,tract_population," +
    "tract_minority_population_percent,ffiec_msa_md_median_family_income,tract_to_msa_income_percentage," +
    "tract_owner_occupied_units,tract_one_to_four_family_homes,tract_median_age_of_housing_units" + "\n"


  def CombinedMLARHeaderPSV: String = """activity_year|lei|loan_type|loan_purpose|preapproval|construction_method|occupancy_type|loan_amount|action_taken|state_code|county_code|census_tract|applicant_ethnicity_1|applicant_ethnicity_2|applicant_ethnicity_3|applicant_ethnicity_4|applicant_ethnicity_5|co_applicant_ethnicity_1|co_applicant_ethnicity_2|co_applicant_ethnicity_3|co_applicant_ethnicity_4|co_applicant_ethnicity_5|applicant_ethnicity_observed|co_applicant_ethnicity_observed|applicant_race_1|applicant_race_2|applicant_race_3|applicant_race_4|applicant_race_5|co_applicant_race_1|co_applicant_race_2|co_applicant_race_3|co_applicant_race_4|co_applicant_race_5|applicant_race_observed|co_applicant_race_observed|applicant_sex|co_applicant_sex|applicant_sex_observed|co_applicant_sex_observed|applicant_age|applicant_age_above_62|co_applicant_age|co_applicant_age_above_62|income|purchaser_type|rate_spread|hoepa_status|lien_status|applicant_credit_scoring_model|co_applicant_credit_scoring_model|denial_reason_1|denial_reason_2|denial_reason_3|denial_reason_4|total_loan_costs|total_points_and_fees|origination_charges|discount_points|lender_credits|interest_rate|prepayment_penalty_term|debt_to_income_ratio|combined_loan_to_value_ratio|loan_term|intro_rate_period|balloon_payment|interest_only_payment|negative_amortization|other_non_amortizing_features|property_value|manufactured_home_secured_property_type|manufactured_home_land_property_interest|total_units|multifamily_affordable_units|submission_of_application|initially_payable_to_institution|aus_1|aus_2|aus_3|aus_4|aus_5|reverse_mortgage|open_end_line_of_credit|business_or_commercial_purpose"""+"\n"
  def CombinedMLARHeaderCSV: String = """activity_year,lei,loan_type,loan_purpose,preapproval,construction_method,occupancy_type,loan_amount,action_taken,state_code,county_code,census_tract,applicant_ethnicity_1,applicant_ethnicity_2,applicant_ethnicity_3,applicant_ethnicity_4,applicant_ethnicity_5,co_applicant_ethnicity_1,co_applicant_ethnicity_2,co_applicant_ethnicity_3,co_applicant_ethnicity_4,co_applicant_ethnicity_5,applicant_ethnicity_observed,co_applicant_ethnicity_observed,applicant_race_1,applicant_race_2,applicant_race_3,applicant_race_4,applicant_race_5,co_applicant_race_1,co_applicant_race_2,co_applicant_race_3,co_applicant_race_4,co_applicant_race_5,applicant_race_observed,co_applicant_race_observed,applicant_sex,co_applicant_sex,applicant_sex_observed,co_applicant_sex_observed,applicant_age,applicant_age_above_62,co_applicant_age,co_applicant_age_above_62,income,purchaser_type,rate_spread,hoepa_status,lien_status,applicant_credit_scoring_model,co_applicant_credit_scoring_model,denial_reason_1,denial_reason_2,denial_reason_3,denial_reason_4,total_loan_costs,total_points_and_fees,origination_charges,discount_points,lender_credits,interest_rate,prepayment_penalty_term,debt_to_income_ratio,combined_loan_to_value_ratio,loan_term,intro_rate_period,balloon_payment,interest_only_payment,negative_amortization,other_non_amortizing_features,property_value,manufactured_home_secured_property_type,manufactured_home_land_property_interest,total_units,multifamily_affordable_units,submission_of_application,initially_payable_to_institution,aus_1,aus_2,aus_3,aus_4,aus_5,reverse_mortgage,open_end_line_of_credit,business_or_commercial_purpose"""+"\n"

}

object ModifiedLHeader extends ModifiedLarHeader {
  def getMLARHeader = {
    MLARHeader
  }
  def getMLARHeaderCSV: String = {
    MLARHeaderCSV
  }
}
