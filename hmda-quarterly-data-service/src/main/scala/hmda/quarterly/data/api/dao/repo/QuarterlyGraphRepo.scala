package hmda.quarterly.data.api.dao.repo

import hmda.model.filing.lar.enums.{ LineOfCreditEnum, LoanTypeEnum }
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

  def fetchApplicationsVolumeByType(loanType: LoanTypeEnum, heloc: Boolean): Task[Seq[ApplicationsVolume]] = {
    val query =
      sql"""
         select last_updated, quarter, sum(agg) agg, #${loanType.code} loan_type from applications_volume
         where loan_type = #${loanType.code} and line_of_credits #${if(heloc) "= 1" else "!= 1"}
         group by last_updated, quarter
         order by quarter
         """.as[ApplicationsVolume]
    Task.deferFuture(db.run(query)).guarantee(Task.shift)
  }

  def fetchLoansVolumeByType(loanType: LoanTypeEnum, heloc: Boolean): Task[Seq[ApplicationsVolume]] = {
    val query =
      sql"""
         select last_updated, quarter, sum(agg) agg, #${loanType.code} loan_type from applications_volume
         where loan_type = #${loanType.code}
            and action_taken_type = 1
            and line_of_credits #${if(heloc) "= 1" else "!= 1"}
         group by last_updated, quarter
         order by quarter
         """.as[ApplicationsVolume]
    Task.deferFuture(db.run(query)).guarantee(Task.shift)
  }
}
