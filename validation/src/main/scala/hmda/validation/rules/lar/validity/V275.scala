package hmda.validation.rules.lar.validity

import hmda.model.fi.lar.LoanApplicationRegister
import hmda.validation.dsl.{ Failure, Result }
import hmda.validation.rules.EditCheck
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._

import scala.util.Try

object V275 extends EditCheck[LoanApplicationRegister] {
  override def apply(lar: LoanApplicationRegister): Result = {
    val actionTakenDate: Int = lar.actionTakenDate
    val dateReceived: String = lar.loan.applicationDate

    when(dateReceived not equalTo("NA")) {
      Try((actionTakenDate - dateReceived.toInt) is greaterThanOrEqual(0))
        .getOrElse(Failure())
    }
  }

  override def name = "V275"
}
