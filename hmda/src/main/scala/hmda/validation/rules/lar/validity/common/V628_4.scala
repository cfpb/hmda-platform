package hmda.validation.rules.lar.validity

import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.filing.lar.enums._
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._
import hmda.validation.dsl.ValidationResult
import hmda.validation.rules.EditCheck

object V628_4 extends EditCheck[LoanApplicationRegister] {
  override def name: String = "V628-4"

  override def parent: String = "V628"

  override def apply(lar: LoanApplicationRegister): ValidationResult = {
    val appEth = lar.applicant.ethnicity
    when(appEth.ethnicity1 is oneOf(InformationNotProvided, EthnicityNotApplicable)) {
      (appEth.ethnicity2 is equalTo(EmptyEthnicityValue)) and
        (appEth.ethnicity3 is equalTo(EmptyEthnicityValue)) and
        (appEth.ethnicity4 is equalTo(EmptyEthnicityValue)) and
        (appEth.ethnicity5 is equalTo(EmptyEthnicityValue))
    }
  }
}
