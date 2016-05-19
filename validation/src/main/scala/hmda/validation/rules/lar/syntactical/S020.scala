package hmda.validation.rules.lar.syntactical

import hmda.model.fi.lar.LoanApplicationRegister
import hmda.validation.dsl.Result
import hmda.validation.rules.EditCheck
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._

object S020 extends EditCheck[LoanApplicationRegister] {

  //Hardcoded for now
  val agencyCodes: List[Int] = List(1, 2, 3, 5, 7, 9)

  override def apply(lar: LoanApplicationRegister): Result = {
    lar.agencyCode is containedIn(agencyCodes)
  }

  override def name = "S020"
}
