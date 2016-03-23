package hmda.validation.rules.lar.syntactical

import hmda.model.fi.lar.LoanApplicationRegister
import hmda.validation.dsl.{ CommonDsl, Result }

object S020 extends CommonDsl {
  //Hardcoded for now
  val agencyCodes: List[Int] = List(1, 2, 3, 5, 7, 9)

  def apply(lar: LoanApplicationRegister): Result = {
    lar.agencyCode is containedIn(agencyCodes)
  }
}
