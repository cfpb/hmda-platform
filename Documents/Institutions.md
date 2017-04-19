# Institutions Data Model

This document describes the data model for Financial Institutions in HMDA. 
A Financial Institution Database is created in HMDA for the purposes of 
filing mortgage data. The information in this database is used in some edits, 
as complementary information for publication and analysis of mortgage data, 
and in the creation of the HMDA Panel. The HMDA Panel is the list of institutions
that are required to file HMDA data, every year. 

The Institutions Database needs to keep historical information for analysis, 
and at least have information readily available for the two previous years in order
to be able to perform the necessary checks when performing validation on the HMDA
data being submitted or when composing the HDMA Panel. In addition to this, it must
maintain the relationships between institutions (i.e. parent/child relationships)
 and the actions that modify them (i.e. mergers, acquisitions). 
 
 
The end product is a list of institutions that contains the necessary information 
so that the HMDA Panel can be processed automatically. This table contains the following
information:


* `Activity Year` (integer length 4, i.e. 2017)
* `Respondent ID` (string length 10)
  Unique identifier for the financial institution for HMDA Panel
* `Respondent RSSD ID` (string)
  Unique identifier for financial institution in NIC. Present for depository
  institutions, may not be present for non-depository institutions
* `Agency Code` (integer, i.e. 9=CFPB)
* `Name` (string)
* `Assets` (double)
* `Other Lender Code` (i.e. 1=Mtg Bkg Sub(MBS))
* `Region` (integer length 2; Agency Office Code)
* `Number of Loans` (integer)
* `Charter Type`
* `Charter Authorization`
* `Deposit Insurance Code`
* `FRB Membership Status`
* `NCUA Membership Status`
* `Legal Entity Identifier (LEI)` (string) (*)
* `Respondent Address`
* `Respondent City`
* `Respondent State`
* `Respondent FIPS State` number (string length 2)
* `Parent Respondent ID`
* `Parent RSSD ID`
* `Parent Name`
* `Parent Address`
* `Parent City`
* `Parent State`
* `Top Holder RSSD ID`
* `Top Holder Name`
* `Top Holder City`
* `Top Holder State`
* `Top Holder Country`
* `HMDA Panel` (integer length 1; 0=no, 1=yes)





(*) These fields will most likely be empty in the 2017 collection.
