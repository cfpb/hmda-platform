package hmda.quarterly.data.api.dao.repo

import hmda.model.filing.lar.enums.{ Conventional, LoanTypeEnum }
import hmda.quarterly.data.api.dao._
import monix.eval.Task
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile
import slick.sql.SqlStreamingAction

object QuarterlyGraphRepo {
  val config = DatabaseConfig.forConfig[JdbcProfile]("db")

  import config._
  import config.profile.api._

  def fetchApplicationsVolumeByType(loanType: LoanTypeEnum, heloc: Boolean, conforming: Boolean): Task[Seq[DataPoint]] = {
    val query =
      sql"""
         select last_updated, quarter, sum(agg) as value from applications_volume
         where loan_type = #${loanType.code}
            and line_of_credits #${if (heloc) "= 1" else "!= 1"}
            #${getAdditionalParams(loanType, conforming)}
         group by last_updated, quarter
         order by quarter
         """.as[DataPoint]
    runQuery(query)
  }

  def fetchLoansVolumeByType(loanType: LoanTypeEnum, heloc: Boolean, conforming: Boolean): Task[Seq[DataPoint]] = {
    val query =
      sql"""
         select last_updated, quarter, sum(agg) as value from applications_volume
         where loan_type = #${loanType.code}
            and action_taken_type = 1
            and line_of_credits #${if (heloc) "= 1" else "!= 1"}
            #${getAdditionalParams(loanType, conforming)}
         group by last_updated, quarter
         order by quarter
         """.as[DataPoint]
    runQuery(query)
  }

  def fetchMedianCreditScoreByType(loanType: LoanTypeEnum, heloc: Boolean, conforming: Boolean): Task[Seq[DataPoint]] = {
    val query =
      sql"""
         select last_updated, quarter, median_credit_score as value from median_credit_score_by_loan_type
         where loan_type = #${loanType.code}
            and loc #${if (heloc) "= 1" else "!= 1"}
            #${getAdditionalParams(loanType, conforming)}
         order by quarter
         """.as[DataPoint]
    runQuery(query)
  }

  def fetchMedianCreditScoreByTypeByRace(loanType: LoanTypeEnum, race: String, conforming: Boolean = false): Task[Seq[DataPoint]] = {
    val query =
      sql"""
         select last_updated, quarter, median_credit_score as value from median_credit_score_by_loan_by_race
         where loan_type = #${loanType.code} and race_ethnicity = '#$race'
            #${getAdditionalParams(loanType, conforming)}
         order by quarter
         """.as[DataPoint]
     runQuery(query)
  }

  private def runQuery[T](query: SqlStreamingAction[Vector[T], T, Effect]) =
    Task.deferFuture(db.run(query)).guarantee(Task.shift)

  private def getAdditionalParams(loanType: LoanTypeEnum, conforming: Boolean): String =
    if (loanType == Conventional) {
      if (conforming) {
        "and cll = 'C'"
      } else {
        "and cll = 'NC'"
      }
    } else {
      ""
    }
}
