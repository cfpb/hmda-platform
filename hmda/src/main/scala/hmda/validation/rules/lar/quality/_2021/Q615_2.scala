package hmda.validation.rules.lar.quality._2021

import hmda.model.filing.lar.LoanApplicationRegister
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._
import hmda.validation.dsl.ValidationResult
import hmda.validation.rules.EditCheck

import scala.util.Try

object Q615_2 extends EditCheck[LoanApplicationRegister] {
  override def name: String = "Q615-2"

  override def parent: String = "Q615"

  override def apply(lar: LoanApplicationRegister): ValidationResult ={
    val totalPoints = lar.loanDisclosure.totalPointsAndFees
    val originationCharges = lar.loanDisclosure.originationCharges

    val oC =
      Try(lar.loanDisclosure.originationCharges.toDouble).getOrElse(0.0)
    val tp = Try(lar.loanDisclosure.totalPointsAndFees.toDouble).getOrElse(0.0)

    when((totalPoints is numeric) and (originationCharges is numeric)
        and (oC not equalTo(0.0)) and (tp not equalTo(0.0))) {
        tp is greaterThan(oC)
    }
  }
}
