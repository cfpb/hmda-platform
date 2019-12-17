-- get the most recent sign_date for every LEI by year 2019
select max_signed.lei,max_signed.submission_id,max_signed.sign_date from (SELECT lei, submission_id, sign_date ,rank() OVER (PARTITION BY lei ORDER BY sign_date DESC) as number
	FROM hmda_user.submission_history sub
	where sub.submission_id like CONCAT('%',sub.lei,'-2019%') and sub.sign_date>0
	group by lei ,submission_id
	order by lei,number asc) max_signed where max_signed.number=1;
-- get the most recent signdate for every LEI by year 2018
select max_signed.lei,max_signed.submission_id,max_signed.sign_date from (SELECT lei, submission_id, sign_date ,rank() OVER (PARTITION BY lei ORDER BY sign_date DESC) as number
	FROM hmda_user.submission_history sub
	where sub.submission_id like CONCAT('%',sub.lei,'-2018%') and sub.sign_date>0
	group by lei ,submission_id
	order by lei,number asc) max_signed where max_signed.number=1;