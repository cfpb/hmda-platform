package hmda.validation.rules.lar.quality

import hmda.model.filing.lar.LoanApplicationRegister
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._
import hmda.validation.dsl.ValidationResult
import hmda.validation.rules.EditCheck

import scala.util.Try

object Q616_2 extends EditCheck[LoanApplicationRegister] {
  override def name: String = "Q616-2"

  override def parent: String = "Q616"

  override def apply(lar: LoanApplicationRegister): ValidationResult = {
    when(
      lar.loanDisclosure.discountPoints not oneOf("NA", "Exempt") and
        (lar.loanDisclosure.totalPointsAndFees not oneOf("NA", "Exempt"))) {
      val dP =
        Try(lar.loanDisclosure.discountPoints.toDouble).getOrElse(0.0)
      val tpf =
        Try(lar.loanDisclosure.totalPointsAndFees.toDouble).getOrElse(0.0)
      tpf is greaterThan(dP)
    }
  }
}
