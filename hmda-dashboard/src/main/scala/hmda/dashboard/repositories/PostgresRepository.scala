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
      print()
    Task.deferFuture(db.run(query)).guarantee(Task.shift)
  }

  def fetchTotalLars(year: Int): Task[Seq[TotalLars]] = {
    val larTable = larTableSelector(year)
    val query = sql"""
        select count(*) from #${larTable}
        where lei NOT IN (#${filterList});
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

  def healthCheck: Task[Unit] = {
    Task.deferFuture (db.run (sql"SELECT 1".as[Int] ) ).guarantee (Task.shift).void
  }
}
