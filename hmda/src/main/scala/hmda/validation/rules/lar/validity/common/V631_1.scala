package hmda.validation.rules.lar.validity

import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.filing.lar.enums._
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._
import hmda.validation.dsl.{ValidationResult, ValidationSuccess, ValidationFailure}
import hmda.validation.rules.EditCheck

object V631_1 extends EditCheck[LoanApplicationRegister] {
  override def name: String = "V631-1"

  override def parent: String = "V631"

  val validEthnicities = List(
    HispanicOrLatino,
    Mexican,
    PuertoRican,
    Cuban,
    OtherHispanicOrLatino,
    NotHispanicOrLatino,
    InformationNotProvided,
    EthnicityNotApplicable,
    EthnicityNoCoApplicant
  )

  override def apply(lar: LoanApplicationRegister): ValidationResult =
    if(lar.coApplicant.ethnicity.otherHispanicOrLatino.isEmpty) {
      lar.coApplicant.ethnicity.ethnicity1 is containedIn(validEthnicities)
    }
    else {
      lar.coApplicant.ethnicity.ethnicity1 is containedIn(validEthnicities) or (lar.coApplicant.ethnicity.ethnicity1 is equalTo(EmptyEthnicityValue))
    }
}
