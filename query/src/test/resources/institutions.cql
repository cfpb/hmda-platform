CREATE TABLE IF NOT EXISTS institutions(
    id varchar PRIMARY KEY,
    agency int,
    period int,
    activity_year int,
    respondent_id varchar,
    type varchar,
    cra boolean,
    email_1 varchar,
    email_2 varchar,
    email_3 varchar,
    respondent_name varchar,
    respondent_state varchar,
    respondent_city varchar,
    respondent_fips varchar,
    hmda_filer boolean,
    parent_respondent_id varchar,
    parent_id_rssd int,
    parent_name varchar,
    parent_city varchar,
    parent_state varchar,
    assets int,
    other_lender_codes int,
    top_holder_id_rssd int,
    top_holder_name varchar,
    top_holder_city varchar,
    top_holder_state varchar,
    top_holder_country varchar
);

INSERT INTO institutions(id,agency,period,activity_year,respondent_id,type,cra,email_1,email_2,email_3,respondent_name,respondent_state,respondent_city,respondent_fips,hmda_filer,parent_respondent_id,parent_id_rssd,parent_name,parent_city,parent_state,assets,other_lender_codes,top_holder_id_rssd,top_holder_name,top_holder_city,top_holder_state,top_holder_country) values('1011674',5,2017,2017,'1117','credit-union',FALSE,'widgetfinancial.com','WIDGETFINANCIAL.COM','','WIDGET FEDERAL CREDIT UNION','PA','ERIE','42',true,'NaN',-1,'','','',262007,0,-1,'','','','');
INSERT INTO institutions(id,agency,period,activity_year,respondent_id,type,cra,email_1,email_2,email_3,respondent_name,respondent_state,respondent_city,respondent_fips,hmda_filer,parent_respondent_id,parent_id_rssd,parent_name,parent_city,parent_state,assets,other_lender_codes,top_holder_id_rssd,top_holder_name,top_holder_city,top_holder_state,top_holder_country) values('1013856',1,2017,2017,'17967','bank',false,'','','','PANOLA NATIONAL BANK','TX','CARTHAGE','48',true,'-1',-1,'PANOLA NATIONAL BANCSHARES, INC.','CARTHAGE','TX',126407,0,-1,'','','','');



