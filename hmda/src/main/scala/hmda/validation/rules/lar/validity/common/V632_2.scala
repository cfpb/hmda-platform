package hmda.validation.rules.lar.validity

import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.filing.lar.enums.{ EmptyEthnicityValue, HispanicOrLatino, NotHispanicOrLatino, VisualOrSurnameEthnicity }
import hmda.validation.dsl.ValidationResult
import hmda.validation.rules.EditCheck
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._

object V632_2 extends EditCheck[LoanApplicationRegister] {
  override def name: String   = "V632-2"
  override def parent: String = "V632"
  override def apply(lar: LoanApplicationRegister): ValidationResult = {
    val coEthnicity      = lar.coApplicant.ethnicity
    val ethnicity1Values = List(HispanicOrLatino, NotHispanicOrLatino)
    val ethnicity2Values =
      List(HispanicOrLatino, NotHispanicOrLatino, EmptyEthnicityValue)
    when(coEthnicity.ethnicityObserved is equalTo(VisualOrSurnameEthnicity)) {
      (coEthnicity.ethnicity1 is containedIn(ethnicity1Values)) and
        (coEthnicity.ethnicity2 is containedIn(ethnicity2Values)) and
        (coEthnicity.ethnicity3 is equalTo(EmptyEthnicityValue)) and
        (coEthnicity.ethnicity4 is equalTo(EmptyEthnicityValue)) and
        (coEthnicity.ethnicity5 is equalTo(EmptyEthnicityValue))
    }
  }
}
