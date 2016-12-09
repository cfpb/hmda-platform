package hmda.validation.rules.lar.quality

import com.typesafe.config.ConfigFactory
import hmda.model.fi.lar.LoanApplicationRegister
import hmda.model.fi.lar.fields.LarTopLevelFields._
import hmda.validation.dsl.Result
import hmda.validation.rules.EditCheck
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._

object Q003 extends EditCheck[LoanApplicationRegister] {
  override def apply(lar: LoanApplicationRegister): Result = {

    val config = ConfigFactory.load()
    val loanAmount = config.getInt("hmda.validation.quality.Q003.loan.amount")

    when(
      (lar.loan.loanType is equalTo(2)) and
        (lar.loan.propertyType is oneOf(1, 2))
    ) {
        lar.loan.amount is lessThanOrEqual(loanAmount)
      }
  }

  override def name = "Q003"

  override def fields(lar: LoanApplicationRegister) = Map(
    noField -> ""
  )

}
