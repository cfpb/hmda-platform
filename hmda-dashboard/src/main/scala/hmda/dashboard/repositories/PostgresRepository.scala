package hmda.dashboard.repositories

import java.text.SimpleDateFormat
import java.util.Calendar

import hmda.dashboard.models._
import monix.eval.Task
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile

class PostgresRepository (config: DatabaseConfig[JdbcProfile],bankFilterList: Array[String] ) {

  import config._
  import config.profile.api._


  private val filterList = bankFilterList.mkString("'","','","'")

  def getDates(y: Int, w: Int) : String = {
    val sdf = new SimpleDateFormat("YYYY-MM-dd")
    val cal = Calendar.getInstance
    cal.set(Calendar.YEAR, y)
    cal.set(Calendar.WEEK_OF_YEAR, w)
    if (w == 1)
      cal.set(Calendar.DAY_OF_YEAR, 1)
    else
      cal.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)
    sdf.format(cal.getTime)
  }

  def tsTableSelector(year: String): String = {
    year match {
      case "2018" => "transmittalsheet2018"
      case "2019" => "transmittalsheet2019"
      case "2020" => "transmittalsheet2020"
      case "2020-Q1" => "ts2020_q1"
      case "2020-Q2" => "ts2020_q2"
      case "2020-Q3" => "ts2020_q3"
      case _    => ""
    }
  }

  def larTableSelector(period: String, mview: String = ""): String = {
    (period, mview) match {
      case ("2018","") => "loanapplicationregister2018"
      case ("2019","") => "loanapplicationregister2019"
      case ("2020","") => "loanapplicationregister2020"
      case ("2018","exemptions") => "exemptions_2018"
      case ("2019","exemptions") => "exemptions_2019"
      case ("2020","exemptions") => "exemptions_2020"
      case ("2018","open_end_credit") => "open_end_credit_filers_by_agency_2018"
      case ("2019","open_end_credit") => "open_end_credit_filers_by_agency_2019"
      case ("2020","open_end_credit") => "open_end_credit_filers_by_agency_2020"
      case ("2018","lar_exemptions") => "lar_count_using_exemption_by_agency_2018"
      case ("2019","lar_exemptions") => "lar_count_using_exemption_by_agency_2019"
      case ("2020","lar_exemptions") => "lar_count_using_exemption_by_agency_2020"
      case ("2018","open_end_credit_lar") => "open_end_credit_lar_count_by_agency_2018"
      case ("2019","open_end_credit_lar") => "open_end_credit_lar_count_by_agency_2019"
      case ("2020","open_end_credit_lar") => "open_end_credit_lar_count_by_agency_2020"
      case ("2020-Q1","") => "lar2020_q1"
      case ("2020-Q2","") => "lar2020_q2"
      case ("2020-Q3","") => "lar2020_q3"
      case _    => ""
    }
  }

  def fetchTotalFilers(period: String): Task[Seq[TotalFilers]] = {
    val tsTable = tsTableSelector(period)
    val query2 = sql"""
        select count(*) from #${tsTable} where upper(lei) not in (#${filterList});
        """.as[TotalFilers]
    Task.deferFuture(db.run(query2)).guarantee(Task.shift)
  }

  def fetchTotalLars(period: String): Task[Seq[TotalLars]] = {
    val tsTable = tsTableSelector(period)
    val query = sql"""
        select SUM(total_lines) from #${tsTable} where upper(lei) NOT IN (#${filterList});
        """.as[TotalLars]
    Task.deferFuture(db.run(query)).guarantee(Task.shift)
  }

  def fetchSingleAttempts(period: String): Task[Seq[SingleAttempts]] = {
    val tsTable = tsTableSelector(period)
    val dollar = "$"
    val query = sql"""
        select count(substring(submission_id,'\d+#${dollar}')::integer) as single_attempts from #${tsTable} a where a.submission_id is not null and substring(submission_id, '\d+#${dollar}')::integer = 1 and upper(lei) NOT IN (#${filterList}) ;
        """.as[SingleAttempts]
    Task.deferFuture(db.run(query)).guarantee(Task.shift)
  }

  def fetchMultipleAttempts(period: String): Task[Seq[MultipleAttempts]] = {
    val tsTable = tsTableSelector(period)
    val dollar = "$"
    val query = sql"""
        select count(substring(submission_id,'\d+#${dollar}')::integer) as multiple_attempts from #${tsTable} a where a.submission_id is not null and substring(submission_id, '\d+#${dollar}')::integer <> 1 and upper(lei) NOT IN (#${filterList});
        """.as[MultipleAttempts]
    Task.deferFuture(db.run(query)).guarantee(Task.shift)
  }

  def fetchTopFilers(count: Int, period: String): Task[Seq[TopFilers]] = {
    val tsTable = tsTableSelector(period)
    val query = sql"""
      select institution_name, lei, total_lines from #${tsTable} where upper(LEI) NOT IN (#${filterList}) order by total_lines desc limit #${count};
      """.as[TopFilers]
    Task.deferFuture(db.run(query)).guarantee(Task.shift)
  }

  def fetchSignsForLastDays(days: Int, period: String): Task[Seq[SignsForLastDays]] = {
    val tsTable = tsTableSelector(period)
    val query = sql"""
      select date(to_timestamp(sign_date/1000)) as signdate, count(*) as numsign from  #${tsTable} where sign_date is not null and upper(lei) NOT IN (#${filterList}) group by date(to_timestamp(sign_date/1000)) order by signdate desc limit #${days};
      """.as[SignsForLastDays]
    Task.deferFuture(db.run(query)).guarantee(Task.shift)
  }

  def fetchFilerAttempts(count: Int, period: String): Task[Seq[FilerAttempts]] = {
    val tsTable = tsTableSelector(period)
    val larTable = larTableSelector(period)
    val dollar = "$"
    val query = sql"""
      select a.institution_name, count(b.lei) as lar_count, substring(submission_id,'\d+#${dollar}')::integer as attempts from #${tsTable} a join #${larTable} b on upper(a.lei) = upper(b.lei) where a.submission_id is not null and upper(a.lei) NOT IN (#${filterList}) group by b.lei, a.submission_id, a.institution_name  order by substring(submission_id, '\d+#${dollar}')::integer DESC LIMIT #${count};
      """.as[FilerAttempts]
    Task.deferFuture(db.run(query)).guarantee(Task.shift)
  }

  def fetchTSRecordCount(period: String): Task[Seq[TSRecordCount]] = {
    val tsTable = tsTableSelector(period)
    val query = sql"""
      select  count(*) from #${tsTable} where upper(lei) NOT IN (#${filterList}) ;
      """.as[TSRecordCount]
    Task.deferFuture(db.run(query)).guarantee(Task.shift)
  }

  def fetchFilersByAgency(period: String): Task[Seq[FilersByAgency]] = {
    val tsTable = tsTableSelector(period)
    val query = sql"""
      select agency, count(*) from #${tsTable} where upper(lei) not in (#${filterList}) group by agency order by agency asc;
      """.as[FilersByAgency]
    Task.deferFuture(db.run(query)).guarantee(Task.shift)
  }

  def fetchLARByAgency(period: String): Task[Seq[LarByAgency]] = {
    val tsTable = tsTableSelector(period)
    val larTable = larTableSelector(period)
    val query = sql"""
      select agency, SUM(total_lines) from #${tsTable} where UPPER(LEI) NOT IN (#${filterList}) group by agency order by agency
      """.as[LarByAgency]
    Task.deferFuture(db.run(query)).guarantee(Task.shift)
  }

  def fetchTopCountiesLar(period: String, count: Int): Task[Seq[TopCountiesLar]] = {
    val tsTable = tsTableSelector(period)
    val larTable = larTableSelector(period)
    val query = sql"""
      select cast(county as varchar), count(*) from #${tsTable} as ts left join #${larTable} as lar on upper(ts.lei) = upper(lar.lei) where lower(county) != 'na' and upper(ts.lei) not in (#${filterList}) group by county order by count(*) desc limit #${count};
      """.as[TopCountiesLar]
      Task.deferFuture(db.run(query)).guarantee(Task.shift)
  }

  def fetchLarCountByPropertyType(period: String): Task[Seq[LarCountByPropertyType]] = {
    val larTable = larTableSelector(period)
    val query = sql"""
      select sum(case when construction_method='1' and cast(lar.total_uits as integer) <=4 then 1 else 0 end) as single_family, sum(case when construction_method='2' and cast(lar.total_uits as integer) <=4 then 1 else 0 end) as manufactured_single_family, sum(case when cast(lar.total_uits as integer) > 4 then 1 else 0 end) as multifamily from #${larTable} as lar where upper(lei) not in (#${filterList});
      """.as[LarCountByPropertyType]
    Task.deferFuture(db.run(query)).guarantee(Task.shift)
  }

  def fetchFilersUsingExemptionByAgency(period: String): Task[Seq[FilersUsingExemptionByAgency]] = {
    val materializedView = larTableSelector(period, "exemptions")
    val query = sql"""
      select agency, count (*) lei from  #${materializedView} where upper(lei) not in (#${filterList}) group by agency
      """.as[FilersUsingExemptionByAgency]
    Task.deferFuture(db.run(query)).guarantee(Task.shift)
  }

  def fetchDenialReasonCountsByAgency(period: String): Task[Seq[DenialReasonCountsByAgency]] = {
    val tsTable = tsTableSelector(period)
    val larTable = larTableSelector(period)
    val query = sql"""
      select agency as agency, sum(case when denial_reason1 = '1' or denial_reason2 = '1' or denial_reason3 = '1' or denial_reason4 = '1' then 1 else 0 end) as dti_ratio, sum(case when denial_reason1 = '2' or denial_reason2 = '2' or denial_reason3 = '2' or denial_reason4 = '2' then 1 else 0 end) as employment_hist, sum(case when denial_reason1 = '3' or denial_reason2 = '3' or denial_reason3 = '3' or denial_reason4 = '3' then 1 else 0 end) as credit_hist, sum(case when denial_reason1 = '4' or denial_reason2 = '4' or denial_reason3 = '4' or denial_reason4 = '4' then 1 else 0 end) as collateral, sum(case when denial_reason1 = '5' or denial_reason2 = '5' or denial_reason3 = '5' or denial_reason4 = '5' then 1 else 0 end) as insufficient_cash, sum(case when denial_reason1 = '6' or denial_reason2 = '6' or denial_reason3 = '6' or denial_reason4 = '6' then 1 else 0 end) as unverified_info, sum(case when denial_reason1 = '7' or denial_reason2 = '7' or denial_reason3 = '7' or denial_reason4 = '7' then 1 else 0 end) as application_incomplete, sum(case when denial_reason1 = '8' or denial_reason2 = '8' or denial_reason3 = '8' or denial_reason4 = '8' then 1 else 0 end) as mortagage_ins_denied, sum(case when denial_reason1 = '9' or denial_reason2 = '9' or denial_reason3 = '9' or denial_reason4 = '9' then 1 else 0 end) as other, sum(case when denial_reason1 = '1111' then 1 else 0 end) as exempt_count from  #${tsTable} as ts left join  #${larTable} as lar on upper(ts.lei) = upper(lar.lei) where upper(ts.lei) not in (#${filterList}) group by  agency order by  agency
      """.as[DenialReasonCountsByAgency]
    Task.deferFuture(db.run(query)).guarantee(Task.shift)
  }

  def fetchLarCountUsingExemptionByAgency(period: String): Task[Seq[LarCountUsingExemptionByAgency]] = {
    val larTable = larTableSelector(period, "lar_exemptions")
    val query = sql"""
      select agency, count(*) from #${larTable} where upper(lei) NOT IN (#${filterList}) group by agency;
      """.as[LarCountUsingExemptionByAgency]
    Task.deferFuture(db.run(query)).guarantee(Task.shift)
  }

  def fetchOpenEndCreditFilersByAgency(period: String): Task[Seq[OpenEndCreditByAgency]] = {
    val larTable = larTableSelector(period, "open_end_credit")
    val query = sql"""
      select agency, count(*) from #${larTable} where upper(lei) NOT IN (#${filterList}) group by agency;
      """.as[OpenEndCreditByAgency]
    Task.deferFuture(db.run(query)).guarantee(Task.shift)
  }

  def fetchOpenEndCreditLarCountByAgency(period: String): Task[Seq[OpenEndCreditLarCountByAgency]] = {
    val larTable = larTableSelector(period, "open_end_credit_lar")
    val query = sql"""
      select agency, count(*) from #${larTable} where upper(lei) NOT IN (#${filterList}) group by agency;
      """.as[OpenEndCreditLarCountByAgency]
    Task.deferFuture(db.run(query)).guarantee(Task.shift)
  }

  def fetchFilersWithOnlyOpenEndCreditTransactions(period: String): Task[Seq[FilersWithOnlyOpenEndCreditTransactions]] = {
    val tsTable = tsTableSelector(period)
    val larTable = larTableSelector(period)
    val query = sql"""
      select agency, count(*) from #${tsTable} as ts where lei NOT IN (#${filterList}) and upper(lei) in ( select upper(lei) from #${larTable} as lar group by lei having sum(case when line_of_credits=2 or line_of_credits=1111 then 1 else 0 end) <=0) group by ts.agency
      """.as[FilersWithOnlyOpenEndCreditTransactions]
    Task.deferFuture(db.run(query)).guarantee(Task.shift)
  }

  def fetchFilersWithOnlyClosedEndCreditTransactions(period: String): Task[Seq[FilersWithOnlyClosedEndCreditTransactions]] = {
    val tsTable = tsTableSelector(period)
    val larTable = larTableSelector(period)
    val query = sql"""
      select ts.agency, count(upper(ts.lei)) from #${tsTable} as ts where ts.lei NOT IN (#${filterList}) and upper(ts.lei) in ( select upper(lei) from #${larTable} as lar group by upper(lar.lei) having sum(case when line_of_credits=1 or line_of_credits=1111 then 1 else 0 end) = 0 and sum(case when line_of_credits=2 then 1 else 0 end) > 0) group by ts.agency;
      """.as[FilersWithOnlyClosedEndCreditTransactions]
    Task.deferFuture(db.run(query)).guarantee(Task.shift)
  }

  def fetchListFilersWithOnlyClosedEndCreditTransactions(period: String): Task[Seq[ListFilersWithOnlyClosedEndCreditTransactions]] = {
    val tsTable = tsTableSelector(period)
    val larTable = larTableSelector(period)
    val query = sql"""
      select ts.agency, ts.institution_name, upper(ts.lei) as lei from #${tsTable} as ts where upper(lei) not in (#${filterList}) and upper(ts.lei) in ( select distinct upper(lar.lei) from #${larTable} as lar group by upper(lar.lei) having sum(case when line_of_credits = 1 or line_of_credits = 1111 then 1 else 0 end) = 0 ) group by upper(ts.lei), ts.agency, ts.institution_name
      """.as[ListFilersWithOnlyClosedEndCreditTransactions]
    Task.deferFuture(db.run(query)).guarantee(Task.shift)
  }

  def fetchFilersListWithOnlyOpenEndCreditTransactions(period: String): Task[Seq[FilersListWithOnlyOpenEndCredit]] = {
    val tsTable = tsTableSelector(period)
    val larTable = larTableSelector(period)
    val query = sql"""
      select ts.lei, ts.agency, ts.institution_name from #${tsTable} as ts left join #${larTable} as lar on upper(ts.lei) = upper(lar.lei) where upper(ts.lei) NOT IN (#${filterList}) group by ts.institution_name, ts.agency, ts.lei having sum(case when line_of_credits = 2 or line_of_credits = 1111 then 1 else 0 end) = 0 and sum(case when line_of_credits = 1 then 1 else 0 end) > 0
      """.as[FilersListWithOnlyOpenEndCredit]
    Task.deferFuture(db.run(query)).guarantee(Task.shift)
  }

  def fetchFilersClaimingExemption(period: String): Task[Seq[FilersClaimingExemption]] = {
    val tsTable = tsTableSelector(period)
    val larTable = larTableSelector(period)
    val query = sql"""
            select lei ,agency ,institution_name from #${tsTable} as ts where upper(ts.lei) not in (#${filterList}) and upper(lei) in ( select upper(lei) from #${larTable} as lar where length(uli) < 23 or lower(street) = 'exempt' or lower(city) = 'exempt' or lower(zip) = 'exempt' or lower(rate_spread) = 'exempt' or credit_score_applicant = '1111' or credit_score_co_applicant = '1111' or credit_score_type_applicant = '1111' or credit_score_type_co_applicant = '1111' or denial_reason1 = '1111' or lower(total_loan_costs) = 'exempt' or lower(total_points) = 'exempt' or lower(origination_charges) = 'exempt' or lower(discount_points) = 'exempt' or lower(lender_credits) = 'exempt' or lower(interest_rate) = 'exempt' or lower(payment_penalty) = 'exempt' or lower(debt_to_incode) = 'exempt' or lower(loan_value_ratio) = 'exempt' or lower(loan_term) = 'exempt' or rate_spread_intro = '1111' or baloon_payment = '1111' or insert_only_payment = '1111' or amortization = '1111' or other_amortization = '1111' or lower(property_value) = 'exempt' or application_submission = '1111' or lan_property_interest = '1111' or lower(mf_affordable) = 'exempt' or home_security_policy = '1111' or payable = '1111' or lower(nmls) = 'exempt' or aus1_result = '1111' or other_aus = '1111' or other_aus_result = '1111' or reverse_mortgage = '1111' or line_of_credits = '1111' or business_or_commercial = '1111') order by agency asc
      """.as[FilersClaimingExemption]
    Task.deferFuture(db.run(query)).guarantee(Task.shift)
  }

  def fetchListQuarterlyFilers(period: String): Task[Seq[ListQuarterlyFilers]] = {
    val tsTable = tsTableSelector(period)
    val larTable = larTableSelector(period)
    val query = sql"""
      select lei, agency, institution_name from #${tsTable} where upper(lei) not in (#${filterList}) and upper(lei) in ( select distinct(upper(lei)) from #${larTable} group by lei having sum(case when action_taken_type != '6' then 1 else 0 end) >= 60000)
      """.as[ListQuarterlyFilers]
    Task.deferFuture(db.run(query)).guarantee(Task.shift)
  }

  def fecthQuarterlyInfo(period: String): Task[Seq[QuarterDetails]] = {
    val tsTable = tsTableSelector(period)
    val larTable = larTableSelector(period)
    val query = sql"""
       select ts.institution_name, lar.lei, sum(case when date(action_taken_date::text) <= date('2020-03-31'::text) then 1 else 0 end) as q1, sum(case when date(action_taken_date::text) > date('2020-03-31'::text) and date(action_taken_date::text) <= date('2020-06-30'::text) then 1 else 0 end) as q2, sum(case when date(action_taken_date::text) > date('2020-06-30'::text) and date(action_taken_date::text) <= date('2020-09-30'::text) then 1 else 0 end) as q3, sum(case when date(action_taken_date::text) >= date('2020-10-01'::text) then 1 else 0 end) as q4 from #${larTable} lar, #${tsTable} ts where lar.lei = ts.lei group by ts.institution_name, lar.lei;
      """.as[QuarterDetails]
    Task.deferFuture(db.run(query)).guarantee(Task.shift)
  }

  def fetchFilersByWeekByAgency(period: String, week: Int): Task[Seq[FilersByWeekByAgency]] = {
    val tsTable = tsTableSelector(period)
    val startDate = getDates(period.toInt+1, week)
    val endDate = getDates(period.toInt+1, week+1)
    val query = sql"""
       select ts.agency, sum(case when to_timestamp(sign_date/1000)::date between '#${startDate}' and '#${endDate}' then 1 else 0 end) as count from #${tsTable} as ts where upper(ts.lei) not in (#${filterList}) group by ts.agency
      """.as[FilersByWeekByAgency]
    Task.deferFuture(db.run(query)).guarantee(Task.shift)
  }

  def fetchLarByWeekByAgency(period: String, week: Int): Task[Seq[LarByWeekByAgency]] = {
    val tsTable = tsTableSelector(period)
    val startDate = getDates(period.toInt+1, week)
    val endDate = getDates(period.toInt+1, week+1)
    val query = sql"""
       select ts.agency ,sum(case when to_timestamp(sign_date/1000)::date between '#${startDate}' and '#${endDate}' then total_lines else 0 end) as count from #${tsTable} as ts where upper(ts.lei) not in (#${filterList}) group by ts.agency
      """.as[LarByWeekByAgency]
    Task.deferFuture(db.run(query)).guarantee(Task.shift)
  }

  def fetchFilersCountClosedEndOriginationsByAgency(period: String, x: Int): Task[Seq[FilersCountClosedEndOriginationsByAgency]] = {
    val tsTable = tsTableSelector(period)
    val larTable = larTableSelector(period)
    val query = sql"""
       select ts.agency ,count(upper(ts.lei)) from #${tsTable} as ts where upper(ts.lei) not in (#${filterList}) and upper(ts.lei) in ( select upper(lei) from #${larTable} group by upper(lei) having sum(case when line_of_credits=2 and action_taken_type=1 then 1 else 0 end) < #${x} ) group by ts.agency
      """.as[FilersCountClosedEndOriginationsByAgency]
    Task.deferFuture(db.run(query)).guarantee(Task.shift)
  }

  def fetchFilersCountClosedEndOriginationsByAgencyGraterOrEqual(period: String, x: Int): Task[Seq[FilersCountClosedEndOriginationsByAgencyGraterOrEqual]] = {
    val tsTable = tsTableSelector(period)
    val larTable = larTableSelector(period)
    val query = sql"""
       select ts.agency ,count(upper(ts.lei)) from #${tsTable} as ts where upper(ts.lei) not in (#${filterList}) and upper(ts.lei) in ( select upper(lei) from #${larTable} group by upper(lei) having sum(case when line_of_credits=2 and action_taken_type=1 then 1 else 0 end) >= #${x} ) group by ts.agency
      """.as[FilersCountClosedEndOriginationsByAgencyGraterOrEqual]
    Task.deferFuture(db.run(query)).guarantee(Task.shift)
  }

  def fetchFilersCountOpenEndOriginationsByAgency(period: String, x: Int): Task[Seq[FilersCountOpenEndOriginationsByAgency]] = {
    val tsTable = tsTableSelector(period)
    val larTable = larTableSelector(period)
    val query = sql"""
        select ts.agency ,count(upper(ts.lei)) from #${tsTable} as ts where upper(ts.lei) not in (#${filterList}) and upper(ts.lei) in ( select upper(lei) from #${larTable} group by upper(lei) having sum(case when line_of_credits=1 and action_taken_type=1 then 1 else 0 end) < #${x}) group by ts.agency
      """.as[FilersCountOpenEndOriginationsByAgency]
    Task.deferFuture(db.run(query)).guarantee(Task.shift)
  }

  def fetchFilersCountOpenEndOriginationsByAgencyGraterOrEqual(period: String, x: Int): Task[Seq[FilersCountOpenEndOriginationsByAgencyGraterOrEqual]] = {
    val tsTable = tsTableSelector(period)
    val larTable = larTableSelector(period)
    val query = sql"""
        select ts.agency ,count(upper(ts.lei)) from #${tsTable} as ts where upper(ts.lei) not in (#${filterList}) and upper(ts.lei) in ( select upper(lei) from #${larTable} group by upper(lei) having sum(case when line_of_credits=1 and action_taken_type=1 then 1 else 0 end) >= #${x}) group by ts.agency
      """.as[FilersCountOpenEndOriginationsByAgencyGraterOrEqual]
    Task.deferFuture(db.run(query)).guarantee(Task.shift)
  }

  def fetchTopInstitutionsCountOpenEndCredit(period: String, x: Int): Task[Seq[TopInstitutionsCountOpenEndCredit]] = {
    val tsTable = tsTableSelector(period)
    val larTable = larTableSelector(period)
    val query = sql"""
         select ts.agency ,upper(ts.lei) ,ts.institution_name from #${larTable} as lar left join #${tsTable} as ts on upper(lar.lei) = upper(ts.lei) where upper(ts.lei) not in (#${filterList}) group by ts.agency, upper(ts.lei) ,ts.institution_name order by rank() over(order by sum(case when line_of_credits=1 then 1 else 0 end) desc) asc limit #${x}
      """.as[TopInstitutionsCountOpenEndCredit]
    Task.deferFuture(db.run(query)).guarantee(Task.shift)
  }

  def fetchLateFilers(period: String, lateDate: String) : Task[Seq[LateFilers]] = {
    val tsTable = tsTableSelector(period)
    val subHistMview = "submission_hist_mview"
    val query = sql"""
         select * from (select ts.agency, ts.institution_name, sh.lei, ts.total_lines, sh.sign_date_east :: date, sh.submission_id, rank() over( partition by sh.lei order by sh.sign_date_east asc) from #${subHistMview} as sh left join #${tsTable} as ts on upper(sh.lei) = upper(ts.lei) where upper(sh.lei) not in ( select distinct upper(lei) from #${subHistMview} as sh_sub where sh_sub.sign_date_utc < '#${lateDate}' and split_part(sh_sub.submission_id, '-', 2) = '#${period}' ) and split_part(sh.submission_id, '-', 2) = '#${period}' order by sh.lei, rank) sl where upper(sl.lei) not in (#${filterList}) and rank=1 order by sign_date_east desc;
      """.as[LateFilers]
    Task.deferFuture(db.run(query)).guarantee(Task.shift)
  }

  def healthCheck: Task[Unit] = {
    Task.deferFuture (db.run (sql"select 1".as[Int] ) ).guarantee (Task.shift).void
  }
}
