package hmda.quarterly.data.api.dao.repo

import hmda.model.filing.lar.enums.LineOfCreditEnum
import hmda.quarterly.data.api.dao._
import hmda.quarterly.data.api.dao.NonStandardLoanType.NonStandardLoanType
import monix.eval.Task
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile

object QuarterlyGraphRepo {
  val config = DatabaseConfig.forConfig[JdbcProfile]("db")

  import config._
  import config.profile.api._

  def fetchLocVolumeByType(lineOfCreditCode: LineOfCreditEnum): Task[Seq[LineOfCreditVolume]] = {
    val query =
      sql"""
         select last_updated, quarter, agg, line_of_credits from loc_volume
         where line_of_credits = #${lineOfCreditCode.code}
         order by quarter
         """.as[LineOfCreditVolume]
    Task.deferFuture(db.run(query)).guarantee(Task.shift)
  }

  def fetchNonStandardLoanVolumeByType(loanType: NonStandardLoanType): Task[Seq[NonStandardLoanVolume]] = {
    val query =
      sql"""
         select last_updated, quarter, '#$loanType' loan_type, sum(agg) volume from non_standard_loan_volume
         where #$loanType = 1
         group by last_updated, quarter
         order by quarter
         """.as[NonStandardLoanVolume]
    Task.deferFuture(db.run(query)).guarantee(Task.shift)
  }
}
