# 2018 LAR Data Dictionary
Field names linked to codesheet below. 


Field Name|Field Data Type|Valid Values|Notes
|----|-------------|----|----|
Record Identifier|Integer|2|
Legal Entity Identifier (LEI)|Character Varying||
[Loan Type](#loan-type)|Integer|1, 2, 3, 4|
[Loan Purpose](#loan-purpose)|Integer|1, 2, 31, 32, 4, 5|
[Preapproval](#preapproval)|Integer|1, 2|
[Construction Method](#construction-method)|Integer|1, 2|
[Occupancy Type](#occupancy-type)|Integer|1, 2, 3|
Loan Amount|Numeric||"Loan amounts are rounded to the midpoint of the nearest $10|000 interval for which the reported value falls (For example a reported value of $117834 would be disclosed as $115000 as the midpoint between values $110000 and $120000)."
[Action Taken](#action-taken)|Integer|1, 2, 3, 4, 5, 6, 7, 8|
State|Character Varying|NA|two-letter state code
County|Character Varying|NA Exempt|state-county FIPS code
Census Tract|Character Varying|NA|11 digit census tract number
[Ethnicity of Applicant or Borrower: 1](#ethnicity-of-applicant-or-borrower)|Character Varying|1, 11, 12, 13, 14, 2, 3, 4|
[Ethnicity of Applicant or Borrower: 2](#ethnicity-of-applicant-or-borrower)|Character Varying|1, 11, 12, 13, 14, 2|
[Ethnicity of Applicant or Borrower: 3](#ethnicity-of-applicant-or-borrower)|Character Varying|1, 11, 12, 13, 14, 2|
[Ethnicity of Applicant or Borrower: 4](#ethnicity-of-applicant-or-borrower)|Character Varying|1, 11, 12, 13, 14, 2|
[Ethnicity of Applicant or Borrower: 5](#ethnicity-of-applicant-or-borrower)|Character Varying|1, 11, 12, 13, 14, 2|
[Ethnicity of Co-Applicant or Co-Borrower: 1](#ethnicity-of-co-applicant-or-co-borrower)|Character Varying|1, 11, 12, 13, 14, 2, 3, 4, 5|
[Ethnicity of Co-Applicant or Co-Borrower: 2](#ethnicity-of-co-applicant-or-co-borrower)|Character Varying|1, 11, 12, 13, 14, 2|
[Ethnicity of Co-Applicant or Co-Borrower: 3](#ethnicity-of-co-applicant-or-co-borrower)|Character Varying|1, 11, 12, 13, 14, 2|
[Ethnicity of Co-Applicant or Co-Borrower: 4](#ethnicity-of-co-applicant-or-co-borrower)|Character Varying|1, 11, 12, 13, 14, 2|
[Ethnicity of Co-Applicant or Co-Borrower: 5](#ethnicity-of-co-applicant-or-co-borrower)|Character Varying|1, 11, 12, 13, 14, 2|
[Ethnicity of Applicant or Borrower Collected on the Basis of Visual Observation or Surname](#ethnicity-of-applicant-or-borrower-collected-on-the-basis-of-visual-observation or-surname)|Integer|1, 2, 3|
[Ethnicity of Co-Applicant or Co-Borrower Collected on the Basis of Visual Observation or Surname](#ethnicity-of-co-applicant-or-co-borrower-collected-on-the-basis-of-visual-observation or-surname)|Integer|1, 2, 3, 4|
[Race of Applicant or Borrower: 1](#race-of-applicant-or-borrower)|Character Varying|1, 2, 21, 22, 23, 24, 25, 26, 27, 3, 4, 41, 42, 43, 44, 5, 6, 7|
[Race of Applicant or Borrower: 2](#race-of-applicant-or-borrower)|Character Varying|1, 2, 21, 22, 23, 24, 25, 26, 27, 3, 4, 41, 42, 43, 44, 5|
[Race of Applicant or Borrower: 3](#race-of-applicant-or-borrower)|Character Varying|1, 2, 21, 22, 23, 24, 25, 26, 27, 3, 4, 41, 42, 43, 44, 5|
[Race of Applicant or Borrower: 4](#race-of-applicant-or-borrower)|Character Varying|1, 2, 21, 22, 23, 24, 25, 26, 27, 3, 4, 41, 42, 43, 44, 5|
[Race of Applicant or Borrower: 5](#race-of-applicant-or-borrower)|Character Varying|1, 2, 21, 22, 23, 24, 25, 26, 27, 3, 4, 41, 42, 43, 44, 5|
[Race of Co-Applicant or Co-Borrower: 1](#race-of-co-applicant-or-co-borrower)|Character Varying|1, 2, 21, 22, 23, 24, 25, 26, 27, 3, 4, 41, 42, 43, 44, 5, 6, 7, 8|
[Race of Co-Applicant or Co-Borrower: 2](#race-of-co-applicant-or-co-borrower)|Character Varying|1, 2, 21, 22, 23, 24, 25, 26, 27, 3, 4, 41, 42, 43, 44, 5|
[Race of Co-Applicant or Co-Borrower: 3](#race-of-co-applicant-or-co-borrower)|Character Varying|1, 2, 21, 22, 23, 24, 25, 26, 27, 3, 4, 41, 42, 43, 44, 5|
[Race of Co-Applicant or Co-Borrower: 4](#race-of-co-applicant-or-co-borrower)|Character Varying|1, 2, 21, 22, 23, 24, 25, 26, 27, 3, 4, 41, 42, 43, 44, 5|
[Race of Co-Applicant or Co-Borrower: 5](#race-of-co-applicant-or-co-borrower)|Character Varying|1, 2, 21, 22, 23, 24, 25, 26, 27, 3, 4, 41, 42, 43, 44, 5|
[Race of Applicant or Borrower Collected on the Basis of Visual Observation or Surname](#race-of-applicant-or-borrower-collected-on-the-basis-of-visual-observation-or-surname)|Integer|1, 2, 3|
[Race of Co-Applicant or Co-Borrower Collected on the Basis of Visual Observation or Surname](#race-of-co-applicant-or-co-borrower-collected-on-the-basis-of-visual-observation-or-surname)|Integer|1, 2, 3, 4|
[Sex of Applicant or Borrower](#sex-of-applicant-or-borrower)|Integer|1, 2, 3, 4, 6|
[Sex of Co-Applicant or Co-Borrower](#sex-of-co-applicant-or-co-borrower)|Integer|1, 2, 3, 4, 5, 6|
[Sex of Applicant or Borrower Collected on the Basis of Visual Observation or Surname](#sex-of-applicant-or-borrower-collected-on-the-basis-of-visual-observation-or-surname)|Integer|1, 2, 3|
[Sex of Co-Applicant or Co-Borrower Collected on the Basis of Visual Observation or Surname](#sex-of-co-applicant-or-co-borrower-collected-on-the-basis-of-visual-observation-or-surname)|Integer|1, 2, 3, 4|
[Age of Applicant or Borrower](#age-of-applicant-or-borrower)|Character Varying|<25, 25-34, 35-44, 45-54, 55-64, 65-74, >74, 8888|
[Age of Applicant or Borrower Greater Than or Equal To 62](#age-of-applicant-or-borrower-greater-than-or-equal-to-62)|Character Varying|Yes, No, NA|
[Age of Co-Applicant or Co-Borrower](#age-of-co-applicant-or-co-borrower)|Character Varying|<25, 25-34, 35-44, 45-54, 55-64, 65-74, >74, 8888, 9999|
[Age of Co-Applicant or Co-Borrower Greater Than or Equal To 62](#age-of-co-applicant-or-co-borrower-greater-than-or-equal-to-62)|Character Varying|Yes, No, NA|
Income|Character Varying|NA|
[Type of Purchaser](#type-of-purchaser)|Integer|0, 1, 2, 3, 4, 5, 6, 71, 72, 8, 9|
Rate Spread|Character Varying|NA, Exempt|
[HOEPA Status](#hoepa-status)|Integer|1, 2, 3|
[Lien Status](#lien-status)|Integer|1, 2|
[Applicant or Borrower - Name and Version of Credit Scoring Model](#applicant-or-borrower-name-and-version-of-credit-scoring-model)|Integer|1, 2, 3, 4, 5, 6, 7, 8, 9, 1111|
[Co-Applicant or Co-Borrower - Name and Version of Credit Scoring Model](#co-applicant-or-co-borrower-name-and-version-of-credit-scoring-model)|Integer|1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 1111|
[Reason for Denial: 1](#reason-for-denial)|Integer|1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 1111|
[Reason for Denial: 2](#reason-for-denial)|Integer|1, 2, 3, 4, 5, 6, 7, 8, 9|
[Reason for Denial: 3](#reason-for-denial)|Integer|1, 2, 3, 4, 5, 6, 7, 8, 9|
[Reason for Denial: 4](#reason-for-denial)|Integer|1, 2, 3, 4, 5, 6, 7, 8, 9|
Total Loan Costs|Character Varying|NA, Exempt|
Total Points and Fees|Character Varying|NA, Exempt|
Origination Charges|Character Varying|NA, Exempt|
Discount Points|Character Varying|NA, Exempt|
Lender Credits|Character Varying|NA, Exempt|
Interest Rate|Character Varying|NA, Exempt|
Prepayment Penalty Term|Character Varying|NA Exempt|
[Debt-to-Income Ratio](#debt-to-income-ratio)|Character Varying|<20%, 20%-<30%, 30%-<36%, 50%-60%, >60%, NA, Exempt|Exact values are reported between 36% and 49%.
Combined Loan-to-Value Ratio|Character Varying|NA, Exempt|
Loan Term|Character Varying|NA, Exempt|
Introductory Rate Period|Character Varying|NA, Exempt|
[Balloon Payment](#balloon-payment)|Integer|1, 2, 1111|
[Interest-Only Payments](#interest-only-payments)|Integer|1, 2, 1111|
[Negative Amortization](#negative-amortization)|Integer|1, 2, 1111|
[Other Non-Amortizing Features](#other-non-amortizing-features)|Integer|1, 2, 1111|
Property Value|Character Varying|NA, Exempt|"Property values are rounded to the midpoint of the nearest $10|000 interval for which the reported value falls (For example a reported value of $117834 would be disclosed as $115000 as the midpoint between values $110000 and $120000)."
[Manufactured Home Secured Property Type](#manufactured-home-secured-property-type)|Integer|1, 2, 3, 1111|
[Manufactured Home Land Property Interest](#manufactured-home-land-property-interest)|Integer|1, 2, 3, 4, 5, 1111|
[Total Units](#total-units)|Character Varying|1, 2, 3, 4, 5-24, 25-49, 50-99, 100-149, >149|
Multifamily Affordable Units|Character Varying|NA Exempt|Listed as a percentage of total units.
[Submission of Application](#submission-of-application)|Integer|1, 2, 3, 1111|
[Initially Payable to Your Institution](#initially-payable)|Integer|1, 2, 3, 1111|
[Automated Underwriting System: 1](#automated-underwriting-system)|Integer|1, 2, 3, 4, 5, 6, 1111|
[Automated Underwriting System: 2](#automated-underwriting-system)|Integer|1, 2, 3, 4, 5|
[Automated Underwriting System: 3](#automated-underwriting-system)|Integer|1, 2, 3, 4, 5|
[Automated Underwriting System: 4](#automated-underwriting-system)|Integer|1, 2, 3, 4, 5|
[Automated Underwriting System: 5](#automated-underwriting-system)|Integer|1, 2, 3, 4, 5|
[Reverse Mortgage](#reverse-mortgage)|Integer|1, 2, 1111|
[Open-End Line of Credit](#open-end-line-of-credit)|Integer|1, 2, 1111|
[Business or Commercial Purpose](#business-or-commercial-purpose)|Integer|1, 2, 1111|
[Conforming Loan Limit Flag](#conforming-loan-limit-flag)|Character Varying|C, NC, U, NA|Indicates whether a loan is Conforming, Nonconforming, Undetermined, or Not Applicable (NA) based on the conforming loan limits determined by FHFA.
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


# 2018 LAR Codesheet
## Loan Type
Code | Description|
|----|-------------|
1 | Conventional (not insured or guaranteed by FHA VA RHS or FSA)
2 | Federal Housing Administration insured (FHA)
3 | Veterans Affairs guaranteed (VA)
4 | USDA Rural Housing Service or Farm Service Agency guaranteed (RHS or FSA)

## Loan Purpose
Code | Description|
|----|-------------|
1 | Home purchase
2 | Home improvement
31 | Veterans Affairs guaranteed (VA)
32 | Cash-out refinancing 
4 | Other purpose
5 | Not applicable

## Preapproval
Code | Description|
|----|-------------|
1 | Preapproval requested	
2 | Preapproval not requested

## Construction Method
Code | Description|
|----|-------------|
1 | Site-built	
2 | Manufactured Home	

## Occupancy Type
Code | Description|
|----|-------------|
1 | Principal residence	
2 | Second residence	
3 | Investment property	

## Action Taken
Code | Description|
|----|-------------|
1 |	Loan originated	
2 |	Application approved but not accepted	
3 |	Application denied	
4 |	Application withdrawn by applicant	
5 |	File closed for incompleteness	
6 |	Purchased loan	
7 |	Preapproval request denied	
8 |	Preapproval request approved but not accepted	

## Ethnicity of Applicant or Borrower
Code | Description|
|----|-------------|
1 | Hispanic or Latino	
11 | Mexican	
12 | Puerto Rican	
13 | Cuban	
14 | Other Hispanic or Latino	
2 | Not Hispanic or Latino	
3 | Information not provided by applicant in mail internet or telephone application*	
4 | Not applicable**	

*Code 3 only valid for Ethnicity of Applicant or Borrower: 1
**Code 4 only valid for Ethnicity of Applicant or Borrower: 1

## Ethnicity of Co-Applicant or Co-Borrower
Code | Description|
|----|-------------|
1 | Hispanic or Latino	
11 | Mexican	
12 | Puerto Rican	
13 | Cuban	
14 | Other Hispanic or Latino	
2 | Not Hispanic or Latino	
3 | Information not provided by applicant in mail internet or telephone application*	
4 | Not applicable**	
5 | No co-applicant***	
*Code 3 only valid for ethnicity of Co-Applicant or Co-Borrower: 1 
**Code 4 only valid for Ethnicity of Co-Applicant or Co-Borrower: 1 
***Code 5 only valid for Ethnicity of Co-Applicant or Co-Borrower: 1 

## Ethnicity of Applicant or Borrower Collected on the Basis of Visual Observation or Surname
Code | Description|
|----|-------------|
1 | Collected on the basis of visual observation or surname	
2 |	Not collected on the basis of visual observation or surname	
3 |	Not applicable	

## Ethnicity of Co-Applicant or Co-Borrower Collected on the Basis of Visual Observation or Surname
Code | Description|
|----|-------------|
1 | Collected on the basis of visual observation or surname	
2 | Not collected on the basis of visual observation or surname	
3 |	Not applicable	
4 |	No co-applicant	

## Race of Applicant or Borrower
Code | Description|
|----|-------------|
1 | American Indian or Alaska Native	
2 | Asian	
21 | Asian Indian	
22 | Chinese	
23 | Filipino	
24 | Japanese	
25 | Korean	
26 | Vietnamese	
27 | Other Asian	
3 | Black or African American	
4 | Native Hawaiian or Other Pacific Islander	
41 | Native Hawaiian	
42 | Guamanian or Chamorro	
43 | Samoan	
44 | Other Pacific Islander	
5 | White	
6 | Information not provided by applicant in mail internet or telephone application*	
7 | Not applicable**	
*Code 6 is only valid for Race of Applicant or Borrower: 1
**Code 7 is only valid for Race of Applicant or Borrower: 1

## Race of Co-Applicant or Co-Borrower
Code | Description|
|----|-------------|
1 | American Indian or Alaska Native	
2 | Asian	
21 | Asian Indian	
22 | Chinese	
23 | Filipino	
24 | Japanese	
25 | Korean	
26 | Vietnamese	
27 | Other Asian	
3 | Black or African American	
4 | Native Hawaiian or Other Pacific Islander	
41 | Native Hawaiian	
42 | Guamanian or Chamorro	
43 | Samoan	
44 | Other Pacific Islander	
5 | White	
6 | Information not provided by applicant in mail internet or telephone application*	
7 | Not applicable**
8 | No co-applicant***
*Code 6 is only valid for Race of Co-Applicant or Co-Borrower: 1
**Code 7 is only valid for Race of Co-Applicant or Co-Borrower: 1
***Code 8 is only valid for Race of Co-Applicant or Co-Borrower: 1

## Race of Applicant or Borrower Collected on the Basis of Visual Observation or Surname
Code | Description|
|----|-------------|
1 | Collected on the basis of visual observation or surname	
2 | Not collected on the basis of visual observation or surname	
3 | Not applicable	

## Race of Co-Applicant or Co-Borrower Collected on the Basis of Visual Observation or Surname
Code | Description|
|----|-------------|
1 | Collected on the basis of visual observation or surname	
2 | Not collected on the basis of visual observation or surname	
3 | Not applicable	
4 | No co-applicant	

## Sex of Applicant or Borrower
Code | Description|
|----|-------------|
1 | Male	
2 | Female	
3 | Information not provided by applicant in mail internet or telephone application	
4 | Not applicable	
6 | Applicant selected both male and female	

## Sex of Co-Applicant or Co-Borrower
Code | Description|
|----|-------------|
1 | Male	
2 | Female	
3 | Information not provided by applicant in mail internet or telephone application	
4 | Not applicable	
5 | No co-applicant
6 | Applicant selected both male and female

## Sex of Applicant or Borrower Collected on the Basis of Visual Observation or Surname
Code | Description|
|----|-------------|
1 | Collected on the basis of visual observation or surname	
2 | Not collected on the basis of visual observation or surname	
3 | Not applicable

## Sex of Co-Applicant or Co-Borrower Collected on the Basis of Visual Observation or Surname
Code | Description|
|----|-------------|
1 | Collected on the basis of visual observation or surname	
2 | Not collected on the basis of visual observation or surname	
3 | Not applicable

## Age of Applicant or Borrower
Code | Description|
|----|-------------|
<25 | Age is less than 25	
25-34 | Age is between 25 and 34	
35-44 | Age is between 35 and 44	
45-54 | Age is between 45 and 54	
55-64 | Age is between 55 and 64	
65-74 | Age is between 65 and 74	
>74	| Age is greater than 74	
8888 | Not applicable

## Age of Applicant or Borrower Greater Than or Equal To 62
Code | Description|
|----|-------------|
Yes | Age or Applicant or Borrower is greater than or equal to 62	
No | Age or Applicant or Borrower is less than 62	
NA | Age or Applicant or Borrower is indicated as 8888

## Age of Co-Applicant or Co-Borrower
Code | Description|
|----|-------------|
<25 | Age is less than 25	
25-34 | Age is between 25 and 34	
35-44 | Age is between 35 and 44	
45-54 | Age is between 45 and 54	
55-64 | Age is between 55 and 64	
65-74 | Age is between 65 and 74	
>74	| Age is greater than 74	
8888 | Not applicable
9999 | No co-applicant	

## Age of Co-Applicant or Co-Borrower Greater Than or Equal To 62
Code | Description|
|----|-------------|
Yes | Age or Co-Applicant or Co-Borrower is greater than or equal to 62	
No | Age or Co-Applicant or Co-Borrower is less than 62	
NA | Age or Co-Applicant or Co-Borrower is indicated as 8888 or 9999

## Type of Purchaser
Code | Description|
|----|-------------|	
0 | Not applicable	
1 | Fannie Mae	
2 | Ginnie Mae	
3 | Freddie Mac	
4  | Farmer Mac	
5 | Private securitizer	
6 | Commercial bank savings bank or savings association	
71 | Credit union mortgage company or finance company	
72 | Life insurance company	
8 | Affiliate institution	
9 | Other type of purchaser	

## HOEPA Status
Code | Description|
|----|-------------|
1 | High-cost mortgage	
2 | Not a high-cost mortgage	
3 | Not applicable

## Lien Status
Code | Description|
|----|-------------|
1 | Secured by a first lien	
2 | Secured by a subordinate lien	

## Applicant or Borrower Name and Version of Credit Scoring Model
Code | Description|
|----|-------------|
1 | Equifax Beacon 5.0	
2 | Experian Fair Isaac	
3 | FICO Risk Score Classic 04	
4 | FICO Risk Score Classic 98	
5 | Vantage Score 2.0	
6 | Vantage Score 3.0	
7 | More than one credit scoring model	
8 | Other credit scoring model	
9 | Not applicable	
1111 | Exempt	

## Co-Applicant or Co-Borrower Name and Version of Credit Scoring Model
Code | Description|
|----|-------------|
1 | Equifax Beacon 5.0	
2 | Experian Fair Isaac	
3 | FICO Risk Score Classic 04	
4 | FICO Risk Score Classic 98	
5 | Vantage Score 2.0	
6 | Vantage Score 3.0	
7 | More than one credit scoring model	
8 | Other credit scoring model	
9 | Not applicable	
10 | No co-applicant	
1111 | Exempt

## Reason for Denial
Code | Description|
|----|-------------|
1 | Debt-to-income ratio	
2 | Employment history	
3 | Credit history	
4 | Collateral	
5 | Insufficient cash (downpayment closing costs)	
6 | Unverifiable information	
7 | Credit application incomplete	
8 | Mortgage insruance denied	
9 | Other	
10 | Not applicable	Code 10 is only valid for Reason for Denial: 1
1111 | ExemptCode 1111 is only valid for Reason for Denial: 1 

## Debt-to-Income Ratio*
Code | Description|
|----|-------------|
<20% | Debt-to-income ratio is less than 20%	
20%-<30% | Debt-to-income ratio is between 20% and is less than 30%	
30%-<36% | Debt-to-income ratio is between 30% and is less than 36%	
50%-60% | Debt-to-income ratio is between 50% and 60%	
60% | Debt-to-income ratio is greater than 60%	
NA | Not applicable	
Exempt | Exemption taken	
*Exact values between 36% and 50% are published with modification	

## Balloon Payment
Code | Description|
|----|-------------|
1 | Balloon payment	
2 | No balloon payment	
1111 | Exempt	

## Interest-Only Payments
Code | Description|
|----|-------------|
1 | Interest-only payments	
2 | No interest-only payments	
1111 | Exempt	

## Negative Amortization
Code | Description|
|----|-------------|
1 | Negative amortization	
2 | No negative amortization	
1111 | Exempt	

## Other Non-Amortizing Features
Code | Description|
|----|-------------|
1 | Other non-fully amortizing features	
2 | No other non-fully amortizing features	
1111 | Exempt	

## Manufactured Home Secured Property Type
Code | Description|
|----|-------------|
1 | Manufactured home and land	
2 | Manufactured home and not land	
3 | Not applicable	
1111 | Exempt	

## Manufactured Home Land Property Interest
Code | Description|
|----|-------------|
1 | Direct ownership	
2 | Indirect ownership	
3 | Paid leasehold	
4 | Unpaid leasehold	
5 | Not applicable	
1111 | Exempt

## Total Units
Code | Description|
|----|-------------|
1 | 1-Unit	
2 | 2-Unit	
3 | 3-Unit	
4 | 4-Unit
5-24 | Total units between 5 and 24
25-49 | Total units between 25 and 49
50-99 | Total units between 50 and 99
100-149 | Total units between 100 and 149
>149 | Total units over 149

## Submission of Application
Code | Description|
|----|-------------|
1 | Submitted directly to your institution
2 | Not submitted directly to your institution
3 | Not applicable
1111 | Exempt

## Initially Payable
Code | Description|
|----|-------------|
1 | Initially payable to your institution
2 | Not initially payable to your institution
3 | Not applicable
1111 | Exempt

## Automated Underwriting System
Code | Description|
|----|-------------|
1 | Desktop Underwriter (DU)
2 | Loan Prospector (LP) or Loan Product Advisor
3 | Technology Open to Approved Lenders (TOTAL) Scorecard
4 | Guaranteed Underwriting System (GUS)
5 | Other
6 | Not applicable
1111 | Exemption taken

## Reverse Mortgage
Code | Description|
|----|-------------|
1 | Reverse mortgage
2 | Not a reverse mortgage
1111 | Exempt

## Open-End Line of Credit
Code | Description|
|----|-------------|
1 | Open-end line of credit
2 | Not an open-end line of credit
1111 | Exempt

## Business or Commercial Purpose
Code | Description|
|----|-------------|
1 | Primarily for a business or commercial purpose
2 | Not primarily for a business or commercial purpose
1111 | Exempt

## Conforming Loan Limit Flag
Code | Description|
|----|-------------|
C  | Conforming
NC | Nonconforming
U | Undetermined
NA | Not Applicable