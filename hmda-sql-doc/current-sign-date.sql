-- get the most recent signdate for every LEI by year 2018
select max_signed.lei,max_signed.submission_id,max_signed.sign_date from (SELECT lei, submission_id, sign_date ,rank() OVER (PARTITION BY lei ORDER BY sign_date DESC) as number
	FROM hmda_user.submission_history sub
	where sub.submission_id like CONCAT('%',sub.lei,'-2018%') and sub.sign_date>0
	group by lei ,submission_id
	order by lei,number asc) max_signed where max_signed.number=1;

-- update transmittal sheet submission_id and sign_date where LEI matches on max sign date

UPDATE hmda_user.ts2018_test dest
SET  submission_id=src.submission_id,
    sign_date=src.sign_date
 FROM (
select max_signed.lei,max_signed.submission_id,max_signed.sign_date from (SELECT lei, submission_id, sign_date ,rank() OVER (PARTITION BY lei ORDER BY sign_date DESC) as number
	FROM hmda_user.submission_history sub
	where sub.submission_id like CONCAT('%',sub.lei,'-2018%') and sub.sign_date>0
	group by lei ,submission_id
	order by lei,number asc) max_signed where max_signed.number=1

) AS src
WHERE dest.lei=src.lei;