package hmda.validation.rules.lar.validity

import hmda.model.fi.lar.LoanApplicationRegister
import hmda.validation.dsl.{ Failure, Result }
import hmda.validation.rules.EditCheck

import scala.util.Try
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._

object V330 extends EditCheck[LoanApplicationRegister] {

  override def apply(lar: LoanApplicationRegister): Result = {
    lar.applicant.income is equalTo("NA") or
      Try(lar.applicant.income.toInt is greaterThan(0))
      .getOrElse(Failure())
  }

  override def name: String = "V330"

}
