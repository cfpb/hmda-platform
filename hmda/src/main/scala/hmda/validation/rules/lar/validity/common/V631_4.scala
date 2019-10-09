package hmda.validation.rules.lar.validity

import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.filing.lar.enums._
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._
import hmda.validation.dsl.ValidationResult
import hmda.validation.rules.EditCheck

object V631_4 extends EditCheck[LoanApplicationRegister] {
  override def name: String = "V631-4"

  override def parent: String = "V631"

  override def apply(lar: LoanApplicationRegister): ValidationResult = {
    val coAppEth = lar.coApplicant.ethnicity
    when(coAppEth.ethnicity1 is oneOf(InformationNotProvided, EthnicityNotApplicable, EthnicityNoCoApplicant)) {
      (coAppEth.ethnicity2 is equalTo(EmptyEthnicityValue)) and
        (coAppEth.ethnicity3 is equalTo(EmptyEthnicityValue)) and
        (coAppEth.ethnicity4 is equalTo(EmptyEthnicityValue)) and
        (coAppEth.ethnicity5 is equalTo(EmptyEthnicityValue))
    }
  }
}
