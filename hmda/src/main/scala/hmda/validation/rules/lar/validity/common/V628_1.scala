package hmda.validation.rules.lar.validity

import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.filing.lar.enums._
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._
import hmda.validation.dsl.ValidationResult
import hmda.validation.rules.EditCheck

object V628_1 extends EditCheck[LoanApplicationRegister] {
  override def name: String = "V628-1"

  override def parent: String = "V628"

  val validEthnicities = List(HispanicOrLatino,
                              Mexican,
                              PuertoRican,
                              Cuban,
                              OtherHispanicOrLatino,
                              NotHispanicOrLatino,
                              InformationNotProvided,
                              EthnicityNotApplicable)

  override def apply(lar: LoanApplicationRegister): ValidationResult =
    if(lar.applicant.ethnicity.otherHispanicOrLatino.isEmpty) {
      lar.applicant.ethnicity.ethnicity1 is containedIn(validEthnicities)
    }
    else {
      lar.applicant.ethnicity.ethnicity1 is containedIn(validEthnicities) or (lar.applicant.ethnicity.ethnicity1 is equalTo(EmptyEthnicityValue))
    }
}
