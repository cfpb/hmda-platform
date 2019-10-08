package hmda.validation.rules.lar.validity

import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.filing.lar.enums.{ EmptyEthnicityValue, HispanicOrLatino, NotHispanicOrLatino, VisualOrSurnameEthnicity }
import hmda.validation.dsl.ValidationResult
import hmda.validation.rules.EditCheck
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._

object V629_2 extends EditCheck[LoanApplicationRegister] {

  override def name: String = "V629-2"

  override def parent: String = "V629"

  override def apply(lar: LoanApplicationRegister): ValidationResult = {
    val ethnicity        = lar.applicant.ethnicity
    val ethnicity1Values = List(HispanicOrLatino, NotHispanicOrLatino)
    val ethnicity2Values =
      List(HispanicOrLatino, NotHispanicOrLatino, EmptyEthnicityValue)
    when(ethnicity.ethnicityObserved is equalTo(VisualOrSurnameEthnicity)) {
      (ethnicity.ethnicity1 is containedIn(ethnicity1Values)) and
        (ethnicity.ethnicity2 is containedIn(ethnicity2Values)) and
        (ethnicity.ethnicity3 is equalTo(EmptyEthnicityValue)) and
        (ethnicity.ethnicity4 is equalTo(EmptyEthnicityValue)) and
        (ethnicity.ethnicity5 is equalTo(EmptyEthnicityValue))
    }
  }

}
