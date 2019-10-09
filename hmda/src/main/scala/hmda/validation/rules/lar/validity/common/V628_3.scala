package hmda.validation.rules.lar.validity

import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.filing.lar.enums._
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._
import hmda.validation.dsl.ValidationResult
import hmda.validation.rules.EditCheck

object V628_3 extends EditCheck[LoanApplicationRegister] {
  override def name: String = "V628-3"

  override def parent: String = "V628"

  override def apply(lar: LoanApplicationRegister): ValidationResult = {
    val appEth       = lar.applicant.ethnicity
    val invalidCodes = List(EmptyEthnicityValue, InvalidEthnicityCode)
    val ethnicities = List(appEth.ethnicity1, appEth.ethnicity2, appEth.ethnicity3, appEth.ethnicity4, appEth.ethnicity5)
      .filterNot(invalidCodes.contains(_))
    ethnicities.distinct.size is equalTo(ethnicities.size)
  }
}
