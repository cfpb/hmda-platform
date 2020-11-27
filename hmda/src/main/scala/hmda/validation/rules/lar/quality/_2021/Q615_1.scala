package hmda.validation.rules.lar.quality._2021

import hmda.model.filing.lar.LoanApplicationRegister
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._
import hmda.validation.dsl.ValidationResult
import hmda.validation.rules.EditCheck

import scala.util.Try

object Q615_1 extends EditCheck[LoanApplicationRegister] {
  override def name: String = "Q615-1"

  override def parent: String = "Q615"

  override def apply(lar: LoanApplicationRegister): ValidationResult ={
    val loanCosts = lar.loanDisclosure.totalLoanCosts
    val originationCharges = lar.loanDisclosure.originationCharges

    val oC =
      Try(lar.loanDisclosure.originationCharges.toDouble).getOrElse(0.0)
    val tlc = Try(lar.loanDisclosure.totalLoanCosts.toDouble).getOrElse(0.0)

    when((loanCosts is numeric) and (originationCharges is numeric)
        and (tlc not equalTo(0.0)) and (oC not equalTo(0.0))) {
        tlc is greaterThan(oC)
    }
  }
}
