package hmda.validation.rules.lar.validity

import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.filing.lar.enums._
import hmda.validation.dsl.ValidationResult
import hmda.validation.rules.EditCheck
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._

object V629_3 extends EditCheck[LoanApplicationRegister] {

  override def name: String = "V629-3"

  override def parent: String = "V629"

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
    val ethnicity = lar.applicant.ethnicity

    when(ethnicity.ethnicityObserved is equalTo(NotVisualOrSurnameEthnicity)) {
      (ethnicity.ethnicity1 is containedIn(validEthnicityList)) or
        (ethnicity.otherHispanicOrLatino not empty)
    }
  }
}
