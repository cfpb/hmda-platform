package hmda.validation.rules.lar.quality._2024

import hmda.model.filing.lar.LoanApplicationRegister
import hmda.validation.dsl.{ValidationFailure, ValidationResult, ValidationSuccess}
import hmda.validation.rules.EditCheck

object Q660_3 extends EditCheck[LoanApplicationRegister] {
  override def name: String = "Q660-3"

  override def parent: String = "Q660"

  override def apply(lar: LoanApplicationRegister): ValidationResult = {

    val specialRegexBeta = ".*[!@$^%*_+={}\\[\\]<>?\\\\].*".r.regex
    val termRegex = ".*(?i:(tbd)).*".r.regex
    val street = lar.geography.street.toLowerCase()

    if (
        street.matches(specialRegexBeta) ||
        street.matches(termRegex)
    ) {
      ValidationFailure
    } else {
      ValidationSuccess
    }
  }
}