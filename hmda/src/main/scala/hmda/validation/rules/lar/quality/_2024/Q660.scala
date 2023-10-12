package hmda.validation.rules.lar.quality._2024

import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.filing.lar.enums._
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._
import hmda.validation.dsl.{ValidationFailure, ValidationResult, ValidationSuccess}
import hmda.validation.rules.EditCheck

import scala.util.Try

object Q660 extends EditCheck[LoanApplicationRegister] {
  override def name: String = "Q660"

  override def apply(lar: LoanApplicationRegister): ValidationResult = {

    val specialRegex =  ".*[~!@#$^%&*_+={}\\[\\];:<,>.?\\\\].*".r.regex
    val termRegex = ".*(?i:(\\btbd\\b)|(\\band\\b)).*".r.regex
    val numOnlyRegex="^(?=[^A-Za-z]+$).*[0-9].*$".r.regex
    val wordOnlyRegex="^[^0-9]+$".r.regex
    val street = lar.geography.street.toLowerCase()
    val exemptPhrases = List("exempt","na","n/a")
    if (
      street.length > 100 ||
      street.matches(specialRegex) ||
      street.matches(termRegex) ||
      street.matches(numOnlyRegex) ||
      (street.matches(wordOnlyRegex) && !exemptPhrases.contains(street))
    ) {
      ValidationFailure
    } else {
      ValidationSuccess
    }
  }
}