DROP MATERIALIZED VIEW IF EXISTS hmda_user.submission_hist_mview;

CREATE MATERIALIZED VIEW hmda_user.submission_hist_mview
AS
  SELECT *,
	  timezone('UTC'::text, to_timestamp((sign_date / 1000)::double precision)) AS sign_date_utc,
    timezone('EST'::text, to_timestamp((sign_date / 1000)::double precision)) AS sign_date_east
  FROM hmda_user.submission_history
WITH DATA;