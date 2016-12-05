package hmda.validation.rules.lar.quality

import com.typesafe.config.ConfigFactory
import hmda.model.fi.lar.LoanApplicationRegister
import hmda.validation.dsl.Result
import hmda.validation.rules.EditCheck
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._

object Q001 extends EditCheck[LoanApplicationRegister] {
  override def apply(lar: LoanApplicationRegister): Result = {

    val config = ConfigFactory.load()
    val loanAmount = config.getInt("hmda.validation.quality.Q001.loan.amount")
    val multiplier = config.getInt("hmda.validation.quality.Q001.incomeMultiplier")

    when(
      (lar.loan.amount is numeric) and
        (lar.applicant.income is numeric)
    ) {
        when(
          (lar.applicant.income.toInt is greaterThan(0)) and
            (lar.loan.amount is greaterThanOrEqual(loanAmount))
        ) {
            lar.loan.amount is lessThan(lar.applicant.income.toInt * multiplier)
          }
      }
  }

  override def name = "Q001"

  override def description = ""
}
