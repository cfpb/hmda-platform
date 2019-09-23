package hmda.validation.rules.lar.validity

import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.filing.lar.enums._
import hmda.validation.dsl.ValidationResult
import hmda.validation.rules.EditCheck
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._

object V632_3 extends EditCheck[LoanApplicationRegister] {
  override def name: String   = "V632-3"
  override def parent: String = "V632"
  val validEthnicityList = List(
    HispanicOrLatino,
    Mexican,
    PuertoRican,
    Cuban,
    OtherHispanicOrLatino,
    NotHispanicOrLatino,
    InformationNotProvided
  )
  override def apply(lar: LoanApplicationRegister): ValidationResult = {
    val coEthnicity = lar.coApplicant.ethnicity
    when(coEthnicity.ethnicityObserved is equalTo(NotVisualOrSurnameEthnicity)) {
      (coEthnicity.ethnicity1 is containedIn(validEthnicityList)) or
        (coEthnicity.ethnicity1 is equalTo(EmptyEthnicityValue) and
          (coEthnicity.otherHispanicOrLatino not empty))

    }
  }
}
