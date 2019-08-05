# 2018 Public LAR Data Dictionary
Field names linked to codesheet below. 


Field Name|Field Data Type|Valid Values|Notes
|----|-------------|----|----|
Record Identifier|Integer|2|
Legal Entity Identifier (LEI)|Character Varying||
Loan Type|Integer|1, 2, 3, 4|
Loan Purpose|Integer|1, 2, 31, 32, 4, 5|
Preapproval|Integer|1, 2|
Construction Method|Integer|1, 2|
Occupancy Type|Integer|1, 2, 3|
Loan Amount|Numeric||Loan amounts are rounded to the midpoint of the nearest $10,000 interval for which the reported value falls (For example a reported value of $117834 would be disclosed as $115000 as the midpoint between values $110000 and $120000).
Action Taken|Integer|1, 2, 3, 4, 5, 6, 7, 8|
State|Character Varying|NA|two-letter state code
County|Character Varying|NA Exempt|state-county FIPS code
Census Tract|Character Varying|NA|11 digit census tract number
Ethnicity of Applicant or Borrower: 1|Character Varying|1, 11, 12, 13, 14, 2, 3, 4|
Ethnicity of Applicant or Borrower: 2|Character Varying|1, 11, 12, 13, 14, 2|
Ethnicity of Applicant or Borrower: 3|Character Varying|1, 11, 12, 13, 14, 2|
Ethnicity of Applicant or Borrower: 4|Character Varying|1, 11, 12, 13, 14, 2|
Ethnicity of Applicant or Borrower: 5|Character Varying|1, 11, 12, 13, 14, 2|
Ethnicity of Co-Applicant or Co-Borrower: 1|Character Varying|1, 11, 12, 13, 14, 2, 3, 4, 5|
Ethnicity of Co-Applicant or Co-Borrower: 2|Character Varying|1, 11, 12, 13, 14, 2|
Ethnicity of Co-Applicant or Co-Borrower: 3|Character Varying|1, 11, 12, 13, 14, 2|
Ethnicity of Co-Applicant or Co-Borrower: 4|Character Varying|1, 11, 12, 13, 14, 2|
Ethnicity of Co-Applicant or Co-Borrower: 5|Character Varying|1, 11, 12, 13, 14, 2|
Ethnicity of Applicant or Borrower Collected on the Basis of Visual Observation or Surname|Integer|1, 2, 3|
Ethnicity of Co-Applicant or Co-Borrower Collected on the Basis of Visual Observation or Surname|Integer|1, 2, 3, 4|
Race of Applicant or Borrower: 1|Character Varying|1, 2, 21, 22, 23, 24, 25, 26, 27, 3, 4, 41, 42, 43, 44, 5, 6, 7|
Race of Applicant or Borrower: 2|Character Varying|1, 2, 21, 22, 23, 24, 25, 26, 27, 3, 4, 41, 42, 43, 44, 5|
Race of Applicant or Borrower: 3|Character Varying|1, 2, 21, 22, 23, 24, 25, 26, 27, 3, 4, 41, 42, 43, 44, 5|
Race of Applicant or Borrower: 4|Character Varying|1, 2, 21, 22, 23, 24, 25, 26, 27, 3, 4, 41, 42, 43, 44, 5|
Race of Applicant or Borrower: 5|Character Varying|1, 2, 21, 22, 23, 24, 25, 26, 27, 3, 4, 41, 42, 43, 44, 5|
Race of Co-Applicant or Co-Borrower: 1|Character Varying|1, 2, 21, 22, 23, 24, 25, 26, 27, 3, 4, 41, 42, 43, 44, 5, 6, 7, 8|
Race of Co-Applicant or Co-Borrower: 2|Character Varying|1, 2, 21, 22, 23, 24, 25, 26, 27, 3, 4, 41, 42, 43, 44, 5|
Race of Co-Applicant or Co-Borrower: 3|Character Varying|1, 2, 21, 22, 23, 24, 25, 26, 27, 3, 4, 41, 42, 43, 44, 5|
Race of Co-Applicant or Co-Borrower: 4|Character Varying|1, 2, 21, 22, 23, 24, 25, 26, 27, 3, 4, 41, 42, 43, 44, 5|
Race of Co-Applicant or Co-Borrower: 5|Character Varying|1, 2, 21, 22, 23, 24, 25, 26, 27, 3, 4, 41, 42, 43, 44, 5|
Race of Applicant or Borrower Collected on the Basis of Visual Observation or Surname|Integer|1, 2, 3|
Race of Co-Applicant or Co-Borrower Collected on the Basis of Visual Observation or Surname|Integer|1, 2, 3, 4|
Sex of Applicant or Borrower|Integer|1, 2, 3, 4, 6|
Sex of Co-Applicant or Co-Borrower|Integer|1, 2, 3, 4, 5, 6|
Sex of Applicant or Borrower Collected on the Basis of Visual Observation or Surname|Integer|1, 2, 3|
Sex of Co-Applicant or Co-Borrower Collected on the Basis of Visual Observation or Surname|Integer|1, 2, 3, 4|
Age of Applicant or Borrower|Character Varying|<25, 25-34, 35-44, 45-54, 55-64, 65-74, >74, 8888|
Age of Applicant or Borrower Greater Than or Equal To 62|Character Varying|Yes, No, NA|
Age of Co-Applicant or Co-Borrower|Character Varying|<25, 25-34, 35-44, 45-54, 55-64, 65-74, >74, 8888, 9999|
Age of Co-Applicant or Co-Borrower Greater Than or Equal To 62|Character Varying|Yes, No, NA|
Income|Character Varying|NA|
Type of Purchaser|Integer|0, 1, 2, 3, 4, 5, 6, 71, 72, 8, 9|
Rate Spread|Character Varying|NA, Exempt|
HOEPA Status|Integer|1, 2, 3|
Lien Status|Integer|1, 2|
Applicant or Borrower - Name and Version of Credit Scoring Model|Integer|1, 2, 3, 4, 5, 6, 7, 8, 9, 1111|
Co-Applicant or Co-Borrower - Name and Version of Credit Scoring Model|Integer|1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 1111|
Reason for Denial: 1|Integer|1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 1111|
Reason for Denial: 2|Integer|1, 2, 3, 4, 5, 6, 7, 8, 9|
Reason for Denial: 3|Integer|1, 2, 3, 4, 5, 6, 7, 8, 9|
Reason for Denial: 4|Integer|1, 2, 3, 4, 5, 6, 7, 8, 9|
Total Loan Costs|Character Varying|NA, Exempt|
Total Points and Fees|Character Varying|NA, Exempt|
Origination Charges|Character Varying|NA, Exempt|
Discount Points|Character Varying|NA, Exempt|
Lender Credits|Character Varying|NA, Exempt|
Interest Rate|Character Varying|NA, Exempt|
Prepayment Penalty Term|Character Varying|NA Exempt|
Debt-to-Income Ratio|Character Varying|<20%, 20%-<30%, 30%-<36%, 50%-60%, >60%, NA, Exempt|Exact values are reported between 36% and 49%.
Combined Loan-to-Value Ratio|Character Varying|NA, Exempt|
Loan Term|Character Varying|NA, Exempt|
Introductory Rate Period|Character Varying|NA, Exempt|
Balloon Payment|Integer|1, 2, 1111|
Interest-Only Payments|Integer|1, 2, 1111|
Negative Amortization|Integer|1, 2, 1111|
Other Non-Amortizing Features|Integer|1, 2, 1111|
Property Value|Character Varying|NA, Exempt|"Property values are rounded to the midpoint of the nearest $10,000 interval for which the reported value falls (For example a reported value of $117834 would be disclosed as $115000 as the midpoint between values $110000 and $120000)."
Manufactured Home Secured Property Type|Integer|1, 2, 3, 1111|
Manufactured Home Land Property Interest|Integer|1, 2, 3, 4, 5, 1111|
Total Units|Character Varying|1, 2, 3, 4, 5-24, 25-49, 50-99, 100-149, >149|
Multifamily Affordable Units|Character Varying|NA Exempt|Listed as a percentage of total units.
Submission of Application|Integer|1, 2, 3, 1111|
Initially Payable to Your Institution|Integer|1, 2, 3, 1111|
Automated Underwriting System: 1|Integer|1, 2, 3, 4, 5, 6, 1111|
Automated Underwriting System: 2|Integer|1, 2, 3, 4, 5|
Automated Underwriting System: 3|Integer|1, 2, 3, 4, 5|
Automated Underwriting System: 4|Integer|1, 2, 3, 4, 5|
Automated Underwriting System: 5|Integer|1, 2, 3, 4, 5|
Reverse Mortgage|Integer|1, 2, 1111|
Open-End Line of Credit|Integer|1, 2, 1111|
Business or Commercial Purpose|Integer|1, 2, 1111|
Conforming Loan Limit Flag|Character Varying|C, NC, U, NA|Indicates whether a loan is Conforming, Nonconforming, Undetermined, or Not Applicable (NA) based on the conforming loan limits determined by FHFA.
Metropolitan Statistical Area/Metropolitan Division (MSA/MD)|Integer||Field derived from Census data.
Population|Character Varying||Field derived from Census data.
Minority Population %|Character Varying||Field derived from Census data.
FFIEC Median Family Income|Character Varying||Field derived from Census data.
Tract to MSA/MD Median Family Income %|Character Varying||Field derived from Census data.
Number of Owner Occupied Units|Character Varying||Field derived from Census data.
Number of 1-to-4 Family Units|Character Varying||Field derived from Census data.
Race Categorization|Character Varying|2 or more minority races, American Indian or Alaska Native, Black or African American, Free Form Text Only, Joint, Native Hawaiian or Other Pacific Islander, Race Not Available, White|Field derived from LAR applicant/borrower and co-applicant/co-borrower race fields. 
Sex Categorization|Character Varying|Female, Joint, Male, Sex Not Available|Field derived from LAR applicant/borrower and co-applicant/co-borrower sex fields. 
Ethnicity Categorization|Character Varying|Ethnicity Not Available, Free Form Text Only, Hispanic or Latino, Joint, Not Hispanic or Latino|Field derived from LAR applicant/borrower and co-applicant/co-borrower ethnicity fields.
