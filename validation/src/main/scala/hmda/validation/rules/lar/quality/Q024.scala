package hmda.validation.rules.lar.quality

import com.typesafe.config.ConfigFactory
import hmda.model.fi.lar.LoanApplicationRegister
import hmda.model.fi.lar.fields.LarTopLevelFields._
import hmda.validation.dsl.Result
import hmda.validation.rules.EditCheck
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._

object Q024 extends EditCheck[LoanApplicationRegister] {
  val config = ConfigFactory.load()
  val minIncome = config.getInt("hmda.validation.quality.Q024.min-income-for-high-loan")

  override def name: String = "Q024"

  override def apply(lar: LoanApplicationRegister): Result = {
    val income = lar.applicant.income

    when((lar.actionTakenType is equalTo(1)) and (income is numeric)) {
      when(lar.loan.amount is greaterThanOrEqual(income.toInt * 5)) {
        income.toInt is greaterThan(minIncome)
      }
    }
  }

  override def fields(lar: LoanApplicationRegister) = Map(
    noField -> ""
  )

}
