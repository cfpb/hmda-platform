# Using Modified LAR Data
Modified LAR is the public-facing set of the Loan Application Register (LAR) data in which 27 fields have been redacted for privacy reasons. In addition, 6 fields have been modified for privacy. Two fields have been added indicating whether the age of the applicant or co-applicant is greater than 62 have been added. Please refer to the [schemas](https://github.com/cfpb/HMDA_Data_Science_Kit/tree/master/documentation_resources/schemas/mlar/schemas/), or [Regulation C](https://www.consumerfinance.gov/policy-compliance/rulemaking/regulations/1003/) for further details on Modified LAR disclosure.

*Note concerning schemas*: Blanks are when a data field contains no entry. Blanks are valid enumerations for some data fields, for more information see the [Filing Instruction Guide](https://s3.amazonaws.com/cfpb-hmda-public/prod/help/2018-hmda-fig-2018-hmda-rule.pdf)

### Opening Modified LAR Text Files With Excel
Modified LAR is available as a pipe-delimited text file with the .txt extension and has no header row. To read this file in Excel, the following steps would need to be taken.

1. Navigate to the Modified Loan/Application Register (LAR) page [here](https://ffiec.cfpb.gov/data-publication/modified-lar/2018). Enter the institution name in the search box, and click `Download Modified LAR`. 

2. Open Excel. Select the `File` menu, and then `Open`. In the Open menu, double-click on `Browse` and view the Downloads folder. Select the modified LAR file in the Downloads folder as shown below. 

![alt text](https://raw.githubusercontent.com/cfpb/HMDA_Data_Science_Kit/master/documentation_resources/example_images/mlar_tutorial_images/Step2.JPG)

3. A window will open that tells Excel how to read the file. Select `Delimited` in the first window and click `Next.` 

![alt text](https://raw.githubusercontent.com/cfpb/HMDA_Data_Science_Kit/master/documentation_resources/example_images/mlar_tutorial_images/Step3.JPG)

4. In the next window, specify the pipe delimiter. Select `Other` under the `Delimiters` column and place a pipe (`|`) character in the text box field. Click `Next.`

![alt text](https://raw.githubusercontent.com/cfpb/HMDA_Data_Science_Kit/master/documentation_resources/example_images/mlar_tutorial_images/Step4.JPG)

5. In the final window, select `General` for data type, and select `Finish.` 
At this point, the data will populate into their own cells.

![alt text](https://raw.githubusercontent.com/cfpb/HMDA_Data_Science_Kit/master/documentation_resources/example_images/mlar_tutorial_images/Step5.JPG)

6. To add a header, right click on the row number to highlight the top row of data, and select `Insert` to add a blank row. 

![alt text](https://raw.githubusercontent.com/cfpb/HMDA_Data_Science_Kit/master/documentation_resources/example_images/mlar_tutorial_images/Step6.JPG)

7. Select the modified LAR header for the appropriate activity year and open in Excel. These headers are available [here](https://github.com/cfpb/HMDA_Data_Science_Kit/tree/master/documentation_resources/schemas/mlar/headers/). To download from Github, click "Raw" at the top of the file.

![alt text](https://raw.githubusercontent.com/cfpb/HMDA_Data_Science_Kit/master/documentation_resources/example_images/mlar_tutorial_images/Step_7_1.JPG) 

Right click the raw text file page and save as a csv in the Downloads folder. 

![alt text](https://raw.githubusercontent.com/cfpb/HMDA_Data_Science_Kit/master/documentation_resources/example_images/mlar_tutorial_images/Step_7_2.JPG)

8. Highlight the first row of the header file, right click, and select `Copy`. 

![alt text](https://raw.githubusercontent.com/cfpb/HMDA_Data_Science_Kit/master/documentation_resources/example_images/mlar_tutorial_images/Step8.JPG)

9. Navigate back to the modified LAR excel sheet, highlight the first row, right click, and select `Insert Copied Cells`. 

![alt text](https://raw.githubusercontent.com/cfpb/HMDA_Data_Science_Kit/master/documentation_resources/example_images/mlar_tutorial_images/Step9.JPG)

### Fields Redacted in the 2018 Modified LAR Data Release for Privacy 
1. `Universal Loan Identifier (ULI) or Non Universal Loan Identifier (NULI)`
2. `Application Date`
3. `Action Taken Date`
4. `Street Address` 
5. `City`
6. `Zip Code`
7. `Ethnicity of Applicant or Borrower: Free Form Text Field for Other 
Hispanic or Latino`
8. `Ethnicity of Co-Applicant or Co-Borrower: Free Form Text Field for Other 
Hispanic or Latino`
9. `Race of Applicant or Borrower: Free Form Text Field for American Indian 
or Alaska Native Enrolled or Principal Tribe`
10. `Race of Applicant or Borrower: Free Form Text Field for Other Asian`
11. `Race of Applicant or Borrower: Free Form Text Field for Other Pacific 
Islander`
12. `Race of Co-Applicant or Co-Borrower: Free Form Text Field for American 
Indian or Alaska Native Enrolled or Principal Tribe`
13. `Race of Co-Applicant or Co-Borrower: Free Form Text Field for Other Asian`
14. `Race of Co-Applicant or Co-Borrower: Free Form Text Field for Other 
Pacific Islander`
15. `Credit Score of Applicant or Borrower.`
16. `Credit Score of Co-Applicant or Co-Borrower`
17. `Applicant or Borrower, Name and Version of Credit Scoring Model: 
Conditional Free Form Text Field for Code 8`
18. `Co-Applicant or Co-Borrower, Name and Version of Credit Scoring Model: 
Conditional Free Form Text Field for Code 8`
19. `Reason for Denial: Conditional Free Form Text Field for Code 9`
20. `Mortgage Loan Originator NMLSR Identifier`
21. `Automated Underwriting System: Conditional Free Form Text Field for Code 5`
22. `Automated Underwriting System Result: 1`
23. `Automated Underwriting System Result: 2`
24. `Automated Underwriting System Result: 3`
25. `Automated Underwriting System Result: 4`
26. `Automated Underwriting System Result: 5`
27. `Automated Underwriting System Result: Conditional Free Form Text Field for 
Code 16`

### New Fields Added to Modified LAR
Age of Applicant >= 62
Description: Field that indicates whether the applicant or borrower has an
age greater than or equal to 62. 
- `Yes`if the age of the applicant is >= 62. 
- `No` if the age is <= 62. 
- `NA`if Age of Applicant or Borrower is 8888 or 9999

Age of Co-Applicant >= 62
Description: Field that indicates whether the applicant or borrower has an
age greater than or equal to 62. 
- `Yes`if the age of the applicant is >= 62. 
- `No` if the age is <= 62. 
- `NA` if Age of Applicant or Borrower is 8888 or 9999

### Fields Modified for Privacy in the Modified LAR Data

The modified LAR data contains the following 
alterations to reported values. 

Loan Amount 
Description: Loan amount are rounded to the midpoint of the nearest $10,000 interval for which the reported value falls (For example a reported value of $117834 would be disclosed as $115000 as the midpoint between values $110000 and $120000). 

Applicant/Co-Applicant Age 
Description: Each age value is binned to the following categories:
- `<25` | Ages less than 25
- `25-34` | Ages between 25 and 34 
- `35-44` | Ages between 35 and 44 
- `45-54` | Ages between 45 and 54 
- `55-64` | Ages between 55 and 64 
- `65-74` | Ages between 65 and 74 
- `>74` | Ages greater than 74 

Debt-to-Income Ratio
Description: Exact values between 36% and less than 50% are published without modification. 
Non-applicable values are published without modification. Exempt values are published without modification. 
All other categories are binned to the following:

- `<20%` | Values less than 20% 
- `20%-<30%` | Values between 20% and less than 30%
- `30%-<36%` | Values between 30% and less than 36% 
- `50%-60%` | Values between 50% and 60% 
- `>60%` | Values greater than 60% 

Property Value 
Description: Property values are rounded to the midpoint of the nearest $10,000 interval for which the reported value falls (For example a reported value of $117834 would be disclosed as $115000 as the midpoint between values $110000 and $120000).

Total Units
Description: Exact values between 1 and 4 are disclosed. Other values are 
binned according to the following: 
- `5-24` | Values between 5 and 24 
- `25-49` | Values between 25 and 49 
- `50-99` | Values between 50 and 99 
- `100-149` | Values between 100 and 149 
- `>149` | Values over 149 

Multifamily Affordable Units
Description: Values that are not NA or Exempt are disclosed as a percentage 
calculated as `multifamily affordable units reported / total units reported,` and rounded to the nearest whole number.  
 
 
