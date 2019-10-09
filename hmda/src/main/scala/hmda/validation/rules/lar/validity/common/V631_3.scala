package hmda.validation.rules.lar.validity

import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.filing.lar.enums._
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._
import hmda.validation.dsl.ValidationResult
import hmda.validation.rules.EditCheck

object V631_3 extends EditCheck[LoanApplicationRegister] {
  override def name: String = "V631-3"

  override def parent: String = "V631"

  override def apply(lar: LoanApplicationRegister): ValidationResult = {
    val coAppEth     = lar.coApplicant.ethnicity
    val invalidCodes = List(EmptyEthnicityValue, InvalidEthnicityCode)
    val ethnicities = List(coAppEth.ethnicity1, coAppEth.ethnicity2, coAppEth.ethnicity3, coAppEth.ethnicity4, coAppEth.ethnicity5)
      .filterNot(invalidCodes.contains(_))
    ethnicities.distinct.size is equalTo(ethnicities.size)
  }
}
