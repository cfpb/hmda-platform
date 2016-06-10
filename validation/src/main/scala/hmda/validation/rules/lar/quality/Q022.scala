package hmda.validation.rules.lar.quality

import hmda.model.fi.lar.LoanApplicationRegister
import hmda.validation.dsl.Result
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

object Q022 {
  def apply(lar: LoanApplicationRegister, year: Int): Result = {
    // This edit should not fail if applicationDate is equal to "NA" or is otherwise non-convertible
    val applicationYear = Try(lar.loan.applicationDate.substring(0,4).toInt).getOrElse(year)

    (year - applicationYear) is between(0, 2)
  }

  def apply(lar: LoanApplicationRegister, fYear: Future[Int])(implicit ec: ExecutionContext): Future[Result] = {
    val applicationYear = Try(lar.loan.applicationDate.substring(0,4).toInt)

    fYear.map(year => {
      (year - applicationYear.getOrElse(year)) is between(0, 2)
    })
  }

  def name = "Q025"
}
