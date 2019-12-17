package hmda.dashboard.repositories

import hmda.dashboard.models._
import monix.eval.Task
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile

class PostgresRepository (tableName: String, config: DatabaseConfig[JdbcProfile]) {

  import config._
  import config.profile.api._

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
        select count(*) from #${yearToFetch} where hmda_filer = true;
        """.as[TotalFilers]
    Task.deferFuture(db.run(query)).guarantee(Task.shift)
  }

  def fetchTotalLars(year: Int): Task[Seq[TotalLars]] = {
    val larTable = larTableSelector(year)
    val query = sql"""
        select count(*) from #${larTable};
        """.as[TotalLars]
    Task.deferFuture(db.run(query)).guarantee(Task.shift)
  }

  def fetchSingleAttempts(year: Int): Task[Seq[SingleAttempts]] = {
    val tsTable = tsTableSelector(year)
    val dollar = "$"
    val query = sql"""
        select count(substring(submission_id,'\d+#${dollar}')::integer) as single_attempts  from #${tsTable} a where a.submission_id is not null and substring(submission_id, '\d+#${dollar}')::integer = 1 ;
        """.as[SingleAttempts]
    Task.deferFuture(db.run(query)).guarantee(Task.shift)
  }

  def fetchMultipleAttempts(year: Int): Task[Seq[MultipleAttempts]] = {
    val tsTable = tsTableSelector(year)
    val dollar = "$"
    val query = sql"""
        select count(substring(submission_id,'\d+#${dollar}')::integer) as multiple_attempts  from #${tsTable} a where a.submission_id is not null and substring(submission_id, '\d+#${dollar}')::integer <> 1 ;
        """.as[MultipleAttempts]
    Task.deferFuture(db.run(query)).guarantee(Task.shift)
  }

  def fetchTopFilers(count: Int, year: Int): Task[Seq[TopFilers]] = {
    val tsTable = tsTableSelector(year)
    val larTable = larTableSelector(year)
    val query = sql"""
      select b.institution_name, a.lei, count(a.lei) from #${larTable} a join #${tsTable} b on a.lei = b.lei group by a.lei, b.institution_name order by count(a.lei) DESC LIMIT #${count};
      """.as[TopFilers]
    Task.deferFuture(db.run(query)).guarantee(Task.shift)
  }

  def fetchSignsForLastDays(days: Int, year: Int): Task[Seq[SignsForLastDays]] = {
    val tsTable = tsTableSelector(year)
    val query = sql"""
      select date(to_timestamp(sign_date/1000)) as signdate, count(*) as numsign from  #${tsTable} where sign_date is not null group by date(to_timestamp(sign_date/1000)) order by signdate desc limit #${days};
      """.as[SignsForLastDays]
    Task.deferFuture(db.run(query)).guarantee(Task.shift)
  }

  def fetchFilerAttempts(count: Int, year: Int): Task[Seq[FilerAttempts]] = {
    val tsTable = tsTableSelector(year)
    val larTable = larTableSelector(year)
    val dollar = "$"
    val query = sql"""
      select a.institution_name, count(b.lei) as lar_count, substring(submission_id,'\d+#${dollar}')::integer as attempts  from #${tsTable} a join #${larTable} b on a.lei = b.lei where a.submission_id is not null group by b.lei, a.submission_id, a.institution_name  order by substring(submission_id, '\d+#${dollar}')::integer DESC LIMIT #${count};
      """.as[FilerAttempts]
    Task.deferFuture(db.run(query)).guarantee(Task.shift)
  }

  def fetchTSRecordCount(year: Int) : Task[Seq[TSRecordCount]] = {
    val tsTable = tsTableSelector(year)
    val query = sql"""
      SELECT  COUNT(*) FROM #${tsTable};
      """.as[TSRecordCount]
    Task.deferFuture(db.run(query)).guarantee(Task.shift)
  }

  def fetchFilersByAgency(year: Int) : Task[Seq[FilersByAgency]] = {
    val tsTable = tsTableSelector(year)
    val query = sql"""
      SELECT agency, COUNT(*) FROM #${tsTable} GROUP BY agency ORDER BY agency ASC;
      """.as[FilersByAgency]
    Task.deferFuture(db.run(query)).guarantee(Task.shift)
  }

  def fetchLARByAgency(year: Int) : Task[Seq[LARByAgency]] = {
    val tsTable = tsTableSelector(year)
    val larTable = larTableSelector(year)
    val query = sql"""
      SELECT ts.agency AS agency, COUNT(lar.*) FROM #${larTable} AS lar JOIN #${tsTable} AS ts ON UPPER(lar.lei) = UPPER(ts.lei) GROUP BY agency;
      """.as[LARByAgency]
    Task.deferFuture(db.run(query)).guarantee(Task.shift)
  }

  def healthCheck: Task[Unit] = {
    Task.deferFuture (db.run (sql"SELECT 1".as[Int] ) ).guarantee (Task.shift).void
  }
}
