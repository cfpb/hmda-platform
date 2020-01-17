package hmda.dashboard.repositories

import hmda.dashboard.models._
import monix.eval.Task
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile

class PostgresRepository (config: DatabaseConfig[JdbcProfile],bankFilterList: Array[String] ) {

  import config._
  import config.profile.api._


  private val filterList = bankFilterList.mkString("'","','","'")

  def tsTableSelector(year: Int): String = {
    year match {
      case 2018 => "transmittalsheet2018"
      case 2019 => "transmittalsheet2019"
      case _    => ""
    }
  }

  def larTableSelector(year: Int): String = {
    year match {
      case 2018 => "loanapplicationregister2018"
      case 2019 => "loanapplicationregister2019"
      case _    => ""
    }
  }

  def fetchTotalFilers(year: Int): Task[Seq[TotalFilers]] = {
    val yearToFetch = year match {
      case 2018 => "institutions2018"
      case 2019 => "institutions2019"
      case _    => ""
    }
    val query = sql"""
        select count(*) from #${yearToFetch} where hmda_filer = true
        and lei not in (#${filterList});
        """.as[TotalFilers]
    Task.deferFuture(db.run(query)).guarantee(Task.shift)
  }

  def fetchTotalLars(year: Int): Task[Seq[TotalLars]] = {
    val larTable = larTableSelector(year)
    val query = sql"""
        select count(*) from #${larTable} where lei NOT IN (#${filterList});
        """.as[TotalLars]
    Task.deferFuture(db.run(query)).guarantee(Task.shift)
  }

  def fetchSingleAttempts(year: Int): Task[Seq[SingleAttempts]] = {
    val tsTable = tsTableSelector(year)
    val dollar = "$"
    val query = sql"""
        select count(substring(submission_id,'\d+#${dollar}')::integer) as single_attempts  from #${tsTable} a where a.submission_id is not null and substring(submission_id, '\d+#${dollar}')::integer = 1 and lei NOT IN (#${filterList}) ;
        """.as[SingleAttempts]
    Task.deferFuture(db.run(query)).guarantee(Task.shift)
  }

  def fetchMultipleAttempts(year: Int): Task[Seq[MultipleAttempts]] = {
    val tsTable = tsTableSelector(year)
    val dollar = "$"
    val query = sql"""
        select count(substring(submission_id,'\d+#${dollar}')::integer) as multiple_attempts from #${tsTable} a where a.submission_id is not null and substring(submission_id, '\d+#${dollar}')::integer <> 1 and lei NOT IN (#${filterList});
        """.as[MultipleAttempts]
    Task.deferFuture(db.run(query)).guarantee(Task.shift)
  }

  def fetchTopFilers(count: Int, year: Int): Task[Seq[TopFilers]] = {
    val tsTable = tsTableSelector(year)
    val larTable = larTableSelector(year)
    val query = sql"""
      select b.institution_name, a.lei, count(a.lei) from #${larTable} a join #${tsTable} b on a.lei = b.lei where a.lei NOT IN (#${filterList}) group by a.lei, b.institution_name order by count(a.lei) DESC LIMIT #${count};
      """.as[TopFilers]
    Task.deferFuture(db.run(query)).guarantee(Task.shift)
  }

  def fetchSignsForLastDays(days: Int, year: Int): Task[Seq[SignsForLastDays]] = {
    val tsTable = tsTableSelector(year)
    val query = sql"""
      select date(to_timestamp(sign_date/1000)) as signdate, count(*) as numsign from  #${tsTable} where sign_date is not null and lei NOT IN (#${filterList}) group by date(to_timestamp(sign_date/1000)) order by signdate desc limit #${days};
      """.as[SignsForLastDays]
    Task.deferFuture(db.run(query)).guarantee(Task.shift)
  }

  def fetchFilerAttempts(count: Int, year: Int): Task[Seq[FilerAttempts]] = {
    val tsTable = tsTableSelector(year)
    val larTable = larTableSelector(year)
    val dollar = "$"
    val query = sql"""
      select a.institution_name, count(b.lei) as lar_count, substring(submission_id,'\d+#${dollar}')::integer as attempts from #${tsTable} a join #${larTable} b on a.lei = b.lei where a.submission_id is not null and a.lei NOT IN (#${filterList}) group by b.lei, a.submission_id, a.institution_name  order by substring(submission_id, '\d+#${dollar}')::integer DESC LIMIT #${count};
      """.as[FilerAttempts]
    Task.deferFuture(db.run(query)).guarantee(Task.shift)
  }

  def fetchTSRecordCount(year: Int): Task[Seq[TSRecordCount]] = {
    val tsTable = tsTableSelector(year)
    val query = sql"""
      select  count(*) from #${tsTable};
      """.as[TSRecordCount]
    Task.deferFuture(db.run(query)).guarantee(Task.shift)
  }

  def fetchFilersByAgency(year: Int): Task[Seq[FilersByAgency]] = {
    val tsTable = tsTableSelector(year)
    val query = sql"""
      select agency, count(*) from #${tsTable} where lei not in (#${filterList}) group by agency order by agency asc;
      """.as[FilersByAgency]
    Task.deferFuture(db.run(query)).guarantee(Task.shift)
  }

  def fetchLARByAgency(year: Int): Task[Seq[LarByAgency]] = {
    val tsTable = tsTableSelector(year)
    val larTable = larTableSelector(year)
    val query = sql"""
      SELECT ts.agency as agency, count(lar.*) from #${larTable} as lar join #${tsTable} as ts on upper(lar.lei) = upper(ts.lei) where ts.lei not in (#${filterList}) group by agency;
      """.as[LarByAgency]
    Task.deferFuture(db.run(query)).guarantee(Task.shift)
  }

  def fetchTopCountiesLar(count: Int, year: Int): Task[Seq[TopCountiesLar]] = {
    val tsTable = tsTableSelector(year)
    val larTable = larTableSelector(year)
    val query = sql"""
      select cast(county as varchar), count(*) from #${tsTable} as ts left join #${larTable} as lar on upper(ts.lei) = upper(lar.lei) where county != 'na' and ts.lei not in (#${filterList}) group by county order by count(*) desc limit #${count};
      """.as[TopCountiesLar]
      Task.deferFuture(db.run(query)).guarantee(Task.shift)
  }

  def fetchLarCountByPropertyType(year: Int): Task[Seq[LarCountByPropertyType]] = {
    val larTable = larTableSelector(year)
    val query = sql"""
      select sum(case when construction_method='1' and cast(lar.total_uits as integer) <=4 then 1 else 0 end) as single_family, sum(case when construction_method='2' and cast(lar.total_uits as integer) <=4 then 1 else 0 end) as manufactured_single_family, sum(case when cast(lar.total_uits as integer) > 4 then 1 else 0 end) as multifamily from #${larTable} as lar where lei not in (#${filterList});
      """.as[LarCountByPropertyType]
    Task.deferFuture(db.run(query)).guarantee(Task.shift)
  }

  def fetchFilersUsingExemptionByAgency(year: Int): Task[Seq[FilersUsingExemptionByAgency]] = {
    val tsTable = tsTableSelector(year)
    val larTable = larTableSelector(year)
    val query = sql"""
      select agency as agency, count(ts.lei) from #${tsTable} as ts where lei not in (#${filterList}) and upper(lei) in (select upper(lei) from #${larTable} as lar where street = 'exempt' or city = 'exempt' or zip_code = 'exempt' or rate_spread = 'exempt' or credit_score_applicant = '1111' or credit_score_co_applicant = '1111' or credit_score_type_applicant = '1111' or credit_score_type_co_applicant = '1111' or denial_reason1 = '1111' or total_loan_costs = 'exempt' or total_points = 'exempt' or origination_charges = 'exempt' or discount_points = 'exempt' or lender_credits = 'exempt' or interest_rate = 'exempt' or payment_penalty = 'exempt' or debt_to_incode = 'exempt' or loan_value_ratio = 'exempt' or loan_term = 'exempt' or rate_spread_intro = '1111' or baloon_payment = '1111' or insert_only_payment = '1111' or amortization = '1111' or other_amortization = '1111' or property_value = 'exempt' or application_submission = '1111' or lan_property_interest = '1111' or mf_affordable = 'exempt' or home_security_policy = '1111' or payable = '1111' or nmls = 'exempt' or aus1_result = '1111' or other_aus = '1111' or other_aus_result = '1111' or reverse_mortgage = '1111' or line_of_credits = '1111' or business_or_commercial = '1111') group by agency order by agency asc
      """.as[FilersUsingExemptionByAgency]
    Task.deferFuture(db.run(query)).guarantee(Task.shift)
  }

  def fetchDenialReasonCountsByAgency(year: Int): Task[Seq[DenialReasonCountsByAgency]] = {
    val tsTable = tsTableSelector(year)
    val larTable = larTableSelector(year)
    val query = sql"""
      select agency as agency, sum(case when denial_reason1 = '1' or denial_reason2 = '1' or denial_reason3 = '1' or denial_reason4 = '1' then 1 else 0 end) as dti_ratio, sum(case when denial_reason1 = '2' or denial_reason2 = '2' or denial_reason3 = '2' or denial_reason4 = '2' then 1 else 0 end) as employment_hist, sum(case when denial_reason1 = '3' or denial_reason2 = '3' or denial_reason3 = '3' or denial_reason4 = '3' then 1 else 0 end) as credit_hist, sum(case when denial_reason1 = '4' or denial_reason2 = '4' or denial_reason3 = '4' or denial_reason4 = '4' then 1 else 0 end) as collateral, sum(case when denial_reason1 = '5' or denial_reason2 = '5' or denial_reason3 = '5' or denial_reason4 = '5' then 1 else 0 end) as insufficient_cash, sum(case when denial_reason1 = '6' or denial_reason2 = '6' or denial_reason3 = '6' or denial_reason4 = '6' then 1 else 0 end) as unverified_info, sum(case when denial_reason1 = '7' or denial_reason2 = '7' or denial_reason3 = '7' or denial_reason4 = '7' then 1 else 0 end) as application_incomplete, sum(case when denial_reason1 = '8' or denial_reason2 = '8' or denial_reason3 = '8' or denial_reason4 = '8' then 1 else 0 end) as mortagage_ins_denied, sum(case when denial_reason1 = '9' or denial_reason2 = '9' or denial_reason3 = '9' or denial_reason4 = '9' then 1 else 0 end) as other, sum(case when denial_reason1 = '1111' then 1 else 0 end) as exempt_count from  #${tsTable} as ts left join  #${larTable} as lar on upper(ts.lei) = upper(lar.lei) where ts.lei not in (#${filterList}) group by  agency order by  agency
      """.as[DenialReasonCountsByAgency]
    Task.deferFuture(db.run(query)).guarantee(Task.shift)
  }

  def fetchLarCountUsingExemptionByAgency(year: Int): Task[Seq[LarCountUsingExemptionByAgency]] = {
    val tsTable = tsTableSelector(year)
    val larTable = larTableSelector(year)
    val query = sql"""
      select agency as agency, count(*) from #${larTable} as lar left join #${tsTable} as ts on UPPER(lar.lei) = UPPER(ts.lei) where ts.lei not in (#${filterList}) and lar.street = 'exempt' or lar.city = 'exempt' or lar.zip = 'exempt' or rate_spread = 'exempt' or credit_score_applicant = '1111' or credit_score_co_applicant = '1111' or credit_score_type_applicant = '1111' or credit_score_type_co_applicant = '1111' or denial_reason1 = '1111' or total_loan_costs = 'exempt' or total_points = 'exempt' or origination_charges = 'exempt' or discount_points = 'exempt' or lender_credits = 'exempt' or interest_rate = 'exempt' or payment_penalty = 'exempt' or debt_to_incode = 'exempt' or loan_value_ratio = 'exempt' or loan_term = 'exempt' or rate_spread_intro = '1111' or baloon_payment = '1111' or insert_only_payment = '1111' or amortization = '1111' or other_amortization = '1111' or property_value = 'exempt' or application_submission = '1111' or lan_property_interest = '1111' or mf_affordable = 'exempt' or home_security_policy = '1111' or payable = '1111' or nmls = 'exempt' or aus1_result = '1111' or other_aus = '1111' or other_aus_result = '1111' or reverse_mortgage = '1111' or line_of_credits = '1111' or business_or_commercial = '1111' group by agency order by agency asc
      """.as[LarCountUsingExemptionByAgency]
    Task.deferFuture(db.run(query)).guarantee(Task.shift)
  }

  def fetchOpenEndCreditFilersByAgency(year: Int): Task[Seq[OpenEndCreditByAgency]] = {
    val tsTable = tsTableSelector(year)
    val larTable = larTableSelector(year)
    val query = sql"""
      select ts.agency, count(*) from #${tsTable} as ts left join #${larTable} as lar on upper(ts.lei) = upper(lar.lei) where ts.lei not in (#${filterList}) and line_of_credits = '1' group by ts.agency
      """.as[OpenEndCreditByAgency]
    Task.deferFuture(db.run(query)).guarantee(Task.shift)
  }

  def fetchFilersWithOnlyOpenEndCreditTransactions(year: Int): Task[Seq[FilersWithOnlyOpenEndCreditTransactions]] = {
    val tsTable = tsTableSelector(year)
    val larTable = larTableSelector(year)
    val query = sql"""
      SELECT agency, count(*) FROM #${tsTable} AS ts WHERE UPPER(lei) IN ( SELECT lei FROM #${larTable} AS lar GROUP BY lei having sum(case when line_of_credits=2 or line_of_credits=1111 then 1 else 0 end) <=0) GROUP BY ts.agency
            """.as[FilersWithOnlyOpenEndCreditTransactions]
    Task.deferFuture(db.run(query)).guarantee(Task.shift)
  }

  // TODO:
  def fetchFilersWithOnlyClosedEndCreditTransactions(year: Int): Task[Seq[FilersWithOnlyClosedEndCreditTransactions]] = {
    val tsTable = tsTableSelector(year)
    val larTable = larTableSelector(year)
    val query = sql"""
      SELECT ts.agency, ts.institution_name FROM #${tsTable} AS ts LEFT JOIN #${larTable} AS lar ON UPPER(ts.lei) = UPPER(lar.lei) GROUP BY ts.lei, ts.agency HAVING SUM(CASE WHEN line_of_credits=2 OR line_of_credits=1111 THEN 1 ELSE 0 END) = 0 AND SUM(CASE WHEN line_of_credits=1 THEN 1 ELSE 0 END) > 0
      """.as[FilersWithOnlyClosedEndCreditTransactions]
    Task.deferFuture(db.run(query)).guarantee(Task.shift)
  }


  def healthCheck: Task[Unit] = {
    Task.deferFuture (db.run (sql"SELECT 1".as[Int] ) ).guarantee (Task.shift).void
  }
}
