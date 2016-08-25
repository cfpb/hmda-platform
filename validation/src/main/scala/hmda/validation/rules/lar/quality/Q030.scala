package hmda.validation.rules.lar.quality

import hmda.model.fi.lar.LoanApplicationRegister
import hmda.model.institution.Institution
import hmda.validation.context.ValidationContext
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateGeo._
import hmda.validation.dsl.PredicateSyntax._
import hmda.validation.dsl.Result
import hmda.validation.rules.{ EditCheck, IfInstitutionPresentIn }

class Q030 private (institution: Institution) extends EditCheck[LoanApplicationRegister] {
  override def name: String = "Q030"

  override def apply(lar: LoanApplicationRegister): Result = {
    when(lar.actionTakenType is containedIn(1 to 6)) {
      // this is a placeholder; there will be more logic here
      lar.geography is smallCounty
    }
  }
}

object Q030 {
  def inContext(context: ValidationContext): EditCheck[LoanApplicationRegister] = {
    IfInstitutionPresentIn(context) { new Q030(_) }
  }
}
