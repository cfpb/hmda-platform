package hmda.publisher.helper

trait LoanLimitLarHeader {

  val LoanLimitHeader = "record_id|lei|uli|application_date|loan_type|loan_purpose|preapprovals|" +
    "construction_method|occupancy|loan_amount|action_type|action_date|address|city|state_code|zip|" +
    "county_code|census_tract_number|applicant_ethnicity_1|applicant_ethnicity_2|applicant_ethnicity_3|" +
    "applicant_ethnicity_4|applicant_ethnicity_5|applicant_ethnicity_other|coapplicant_ethnicity_1|" +
    "coapplicant_ethnicity_2|coapplicant_ethnicity_3|coapplicant_ethnicity_4|coapplicant_ethnicity_5|" +
    "coapplicant_ethnicity_other|applicant_ethnicity_v|coapplicant_ethnicity_v|applicant_race1|" +
    "applicant_race2|applicant_race3|applicant_race4|applicant_race5|applicant_race_other1|" +
    "applicant_race_other2|applicant_race_other3|coapplicant_race1|coapplicant_race2|coapplicant_race3|" +
    "coapplicant_race4|coapplicant_race5|coapplicant_race_other1|coapplicant_race_other2|coapplicant_race_other3|" +
    "applicant_race_v|coapplicant_race_v|applicant_sex|coapplicant_sex|applicant_sex_v|coapplicant_sex_v|" +
    "applicant_age|coapplicant_age|applicant_income|purchaser_type|rate_spread|hoepa_status|lien_status|" +
    "applicant_creditscore|coapplicant_creditscore|applicant_creditscore_model|applicant_creditscore_model_c|" +
    "coapplicant_creditscore_model|coapplicant_creditscore_model_c|denial_reason1|denial_reason2|denial_reason3|" +
    "denial_reason4|denial_reason5|total_loan_costs|total_points_fees|origination_charges|discount_points|" +
    "lender_credits|interest_rate|prepayment_penalty_term|debt_to_income_ratio|combinedloan_to_value_ratio|" +
    "loan_term|introductory_rate_period|balloon_payment|interest_only_payments|negative_amortization|" +
    "other_nonamortizing_features|property_value|manufacturedhome_secured_property_type|" +
    "manufacturedhome_land_property_interest|total_units_interest|multifamily_affordable_units|submission_of_application|" +
    "initially_payable|nmlsr_identifier|autounderwriting_sys1|autounderwriting_sys2|autounderwriting_sys3|" +
    "autounderwriting_sys4|autounderwriting_sys5|autounderwriting_sys_other|autounderwriting_sys_result1|" +
    "autounderwriting_sys_result2|autounderwriting_sys_result3|autounderwriting_sys_result4|" +
    "autounderwriting_sys_result5|autounderwriting_sys_result_other|reverse_mortgage|openend_line_of_credit|" +
    "business_commercial_purpose|conforming_loan_limit|population|minority_population_percent|median_income_percentage|" +
    "tract_to_msamd|owner_occupied_units|one_to_four_fam_units|median_age|msa_md|msa_md_name"
}

object LoanLimitHeader extends LoanLimitLarHeader {
  def getHeaderString = {
    LoanLimitHeader
  }

}

