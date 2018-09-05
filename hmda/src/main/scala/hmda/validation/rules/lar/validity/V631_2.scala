package hmda.validation.rules.lar.validity

import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.filing.lar.enums._
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._
import hmda.validation.dsl.ValidationResult
import hmda.validation.rules.EditCheck

object V631_2 extends EditCheck[LoanApplicationRegister] {
  override def name: String = "V631-2"

  override def parent: String = "V631"

  val validEthnicities = List(EmptyEthnicityValue,
                              HispanicOrLatino,
                              Mexican,
                              PuertoRican,
                              Cuban,
                              OtherHispanicOrLatino,
                              NotHispanicOrLatino)

  override def apply(lar: LoanApplicationRegister): ValidationResult = {
    (lar.coApplicant.ethnicity.ethnicity2 is containedIn(validEthnicities)) and
      (lar.coApplicant.ethnicity.ethnicity3 is containedIn(validEthnicities)) and
      (lar.coApplicant.ethnicity.ethnicity4 is containedIn(validEthnicities)) and
      (lar.coApplicant.ethnicity.ethnicity5 is containedIn(validEthnicities))
  }
}
