package hmda.validation.rules.lar.validity

import hmda.model.fi.lar.LoanApplicationRegister
import hmda.validation.dsl.Result
import hmda.validation.rules.EditCheck
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._

import scala.util.Try

object V210 extends EditCheck[LoanApplicationRegister] {
  override def apply(lar: LoanApplicationRegister): Result = {
    val date = lar.loan.applicationDate

    if (date.length != 8 && Try(date.toInt).isFailure) {
      return lar.loan.applicationDate is equalTo("NA")
    }

    validCentury(date.substring(0, 2)) and
      validMonth(date.substring(4, 6)) and
      validDay(date.substring(6))
  }

  private def validCentury(s: String): Result = {
    s is equalTo("20")
  }

  private def validMonth(s: String): Result = {
    val month = Try(s.toInt).getOrElse(-1)
    (month is lessThanOrEqual(12)) and (month is greaterThanOrEqual(1))
  }

  private def validDay(s: String): Result = {
    val day = Try(s.toInt).getOrElse(-1)
    (day is lessThanOrEqual(31)) and (day is greaterThanOrEqual(1))
  }

  override def name = "V210"
}
