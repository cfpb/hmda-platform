package hmda.validation.rules.lar.validity

import hmda.model.fi.lar.LoanApplicationRegister
import hmda.validation.dsl.{ Failure, Result }
import hmda.validation.rules.EditCheck

import scala.util.Try

object V330 extends EditCheck[LoanApplicationRegister] {

  def apply(lar: LoanApplicationRegister): Result = {
    lar.applicant.income is equalTo("NA") or
      Try(lar.applicant.income.toInt is greaterThan(0))
      .getOrElse(Failure(s"Can't parse '${lar.applicant.income}' as an Int"))
  }

  def name: String = "V330"

}