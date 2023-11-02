package hmda.validation.rules.lar.quality._2024

import hmda.model.filing.lar.LoanApplicationRegister
import hmda.validation.dsl.{ValidationFailure, ValidationResult, ValidationSuccess}
import hmda.validation.rules.EditCheck

object Q660_4 extends EditCheck[LoanApplicationRegister] {
  override def name: String = "Q660-4"

  override def parent: String = "Q660"

  override def apply(lar: LoanApplicationRegister): ValidationResult = {


    val numOnlyRegex = "^(?=[^A-Za-z]+$).*[0-9].*$".r.regex
    val wordOnlyRegex = "^[^0-9]+$".r.regex
    val street = lar.geography.street.toLowerCase()
    val exemptPhrases = List("exempt", "na")
    if (
        street.matches(numOnlyRegex) ||
        (street.matches(wordOnlyRegex) && !exemptPhrases.contains(street))
    ) {
      ValidationFailure
    } else {
      ValidationSuccess
    }
  }
}