package hmda.validation.rules.lar.quality

import hmda.model.fi.lar.{ Geography, LoanApplicationRegister }
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
      when(lar.geography is Geography("NA", "NA", "NA", "NA")) {
        institution.cra is false
      } and
        when(lar.geography.state is "NA") {
          lar.geography.county is "NA"
        } and
        when(lar.geography.county is "NA") {
          lar.geography.state is "NA"
        } and
        when(institution.cra is true) {
          (lar.geography.state not "NA") and (lar.geography.county not "NA")
        }
    }
  }
}

object Q030 {
  def inContext(context: ValidationContext): EditCheck[LoanApplicationRegister] = {
    IfInstitutionPresentIn(context) { new Q030(_) }
  }
}
