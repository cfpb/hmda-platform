package hmda.validation.rules.lar.validity

import hmda.model.fi.lar.LoanApplicationRegister
import hmda.validation.dsl.Result
import hmda.validation.rules.EditCheck
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._

object V326 extends EditCheck[LoanApplicationRegister] {
  override def name: String = "V326"

  override def apply(lar: LoanApplicationRegister): Result = {
    when((lar.applicant.coRace1 is equalTo(8)) or (lar.applicant.coEthnicity is equalTo(5))) {
      lar.applicant.coSex is equalTo(5)
    }
  }
}
