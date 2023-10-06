package hmda.validation.rules.lar.quality._2023

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

    /*
    Trigger Conditions:

      1). The Street Address is more than 100 characters.
      2) The Street Address includes the values: “;”, “&”, or “and“.
      3) The reported Street Address includes the values: ?, !, *, %, ^, @, [, ], {, } ,<,>_, +, = or “tbd”.
      4) The reported Street Address does not contain numbers or contains only numbers.
     */
    val specialRegex =  ".*[~!@#$^%&*_+={}\\[\\];:<,>.?\\\\].*".r.regex
    val termRegex = ".*(?i:(\\btbd\\b)|(\\band\\b)).*".r.regex
    val numOnlyRegex="^(?=[^A-Za-z]+$).*[0-9].*$".r.regex
    val wordOnlyRegex="^[^0-9]+$".r.regex
    val street = lar.geography.street

    if (
      street.length > 100 ||
      street.matches(specialRegex) ||
      street.matches(termRegex) ||
      street.matches(numOnlyRegex) ||
      street.matches(wordOnlyRegex) ||
    ) {
      ValidationFailure
    } else {

      ValidationSuccess
    }
  }
}