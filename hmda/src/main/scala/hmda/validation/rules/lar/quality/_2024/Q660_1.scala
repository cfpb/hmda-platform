package hmda.validation.rules.lar.quality._2024

import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.filing.lar.enums._
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._
import hmda.validation.dsl.{ValidationFailure, ValidationResult, ValidationSuccess}
import hmda.validation.rules.EditCheck

import scala.util.Try

object Q660_1 extends EditCheck[LoanApplicationRegister] {
  override def name: String = "Q660-1"

  override def parent: String = "Q660"
  override def apply(lar: LoanApplicationRegister): ValidationResult = {


    val street = lar.geography.street.toLowerCase()

    if (street.length > 100) {
      ValidationFailure
    } else {
      ValidationSuccess
    }
  }
}