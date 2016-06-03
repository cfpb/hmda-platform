package hmda.validation.rules.lar.quality

import com.typesafe.config.ConfigFactory
import hmda.model.fi.lar.LoanApplicationRegister
import hmda.validation.dsl.Result
import hmda.validation.rules.EditCheck
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._

object Q014 extends EditCheck[LoanApplicationRegister] {
  val config = ConfigFactory.load()
  val max_income = config.getInt("hmda.validation.quality.Q014.applicant.max_income")

  override def name: String = "Q014"

  override def apply(lar: LoanApplicationRegister): Result = {
    val income = lar.applicant.income
    when(income is numeric) {
      income.toInt is lessThan(max_income)
    }
  }
}
