CREATE OR REPLACE FUNCTION refresh_mviews_2018() RETURNS trigger AS $function$
BEGIN
  REFRESH MATERIALIZED VIEW exemptions_2018;
  REFRESH MATERIALIZED VIEW lar_count_using_exemption_by_agency_2018;
  REFRESH MATERIALIZED VIEW open_end_credit_filers_by_agency_2018;
  REFRESH MATERIALIZED VIEW open_end_credit_lar_count_by_agency_2018;
  REFRESH MATERIALIZED VIEW ts_mview_2018;
  REFRESH MATERIALIZED VIEW submission_hist_mview;
  RETURN NULL;
END;
$function$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION refresh_mviews_2019() RETURNS trigger AS $function$
BEGIN
  REFRESH MATERIALIZED VIEW exemptions_2019;
  REFRESH MATERIALIZED VIEW lar_count_using_exemption_by_agency_2019;
  REFRESH MATERIALIZED VIEW open_end_credit_filers_by_agency_2019;
  REFRESH MATERIALIZED VIEW open_end_credit_lar_count_by_agency_2019;
  REFRESH MATERIALIZED VIEW ts_mview_2019;
  REFRESH MATERIALIZED VIEW submission_hist_mview;
  RETURN NULL;
END;
$function$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION refresh_mviews_2020() RETURNS trigger AS $function$
BEGIN
  REFRESH MATERIALIZED VIEW exemptions_2020;
  REFRESH MATERIALIZED VIEW lar_count_using_exemption_by_agency_2020;
  REFRESH MATERIALIZED VIEW open_end_credit_filers_by_agency_2020;
  REFRESH MATERIALIZED VIEW open_end_credit_lar_count_by_agency_2020;
  REFRESH MATERIALIZED VIEW submission_hist_mview;
  RETURN NULL;
END;
$function$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION refresh_mviews_2021() RETURNS trigger AS $function$
BEGIN
  REFRESH MATERIALIZED VIEW exemptions_2021;
  REFRESH MATERIALIZED VIEW lar_count_using_exemption_by_agency_2021;
  REFRESH MATERIALIZED VIEW open_end_credit_filers_by_agency_2021;
  REFRESH MATERIALIZED VIEW open_end_credit_lar_count_by_agency_2021;
  REFRESH MATERIALIZED VIEW submission_hist_mview;
  RETURN NULL;
END;
$function$ LANGUAGE plpgsql;

CREATE TRIGGER refresh_mviews_2018
AFTER INSERT OR UPDATE OR DELETE ON hmda_user.loanapplicationregister2018 
FOR EACH STATEMENT
EXECUTE PROCEDURE refresh_mviews_2018();

CREATE TRIGGER refresh_mviews_2019
AFTER INSERT OR UPDATE OR DELETE ON hmda_user.loanapplicationregister2019 
FOR EACH STATEMENT
EXECUTE PROCEDURE refresh_mviews_2019();

CREATE TRIGGER refresh_mviews_2020
AFTER INSERT OR UPDATE OR DELETE ON hmda_user.loanapplicationregister2020 
FOR EACH STATEMENT
EXECUTE PROCEDURE refresh_mviews_2020();

CREATE TRIGGER refresh_mviews_2021
AFTER INSERT OR UPDATE OR DELETE ON hmda_user.loanapplicationregister2021
FOR EACH STATEMENT
EXECUTE PROCEDURE refresh_mviews_2021();

/* 
 * See all triggers
 */
SELECT * FROM information_schema.triggers;

/* Drop trigger
 * 
 */
DROP TRIGGER refresh_mviews_2018 on loanapplicationregister2018;
DROP TRIGGER refresh_mviews_2019 on loanapplicationregister2019;
DROP TRIGGER refresh_mviews_2020 on loanapplicationregister2020;
DROP TRIGGER refresh_mviews_2021 on loanapplicationregister2021;

