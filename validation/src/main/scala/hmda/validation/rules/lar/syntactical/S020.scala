package hmda.validation.rules.lar.syntactical

import hmda.model.fi.lar.LoanApplicationRegister
import hmda.validation.dsl.{ CommonDsl, Result }
import hmda.validation.rules.EditCheck

object S020 extends EditCheck[LoanApplicationRegister] {
  //Hardcoded for now
  val agencyCodes: List[Int] = List(1, 2, 3, 5, 7, 9)

  def apply(lar: LoanApplicationRegister): Result = {
    lar.agencyCode is containedIn(agencyCodes)
  }

  def name = "S020"
}
