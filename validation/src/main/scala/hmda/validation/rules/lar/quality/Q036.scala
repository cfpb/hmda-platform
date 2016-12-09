package hmda.validation.rules.lar.quality

import com.typesafe.config.ConfigFactory
import hmda.model.fi.lar.LoanApplicationRegister
import hmda.model.fi.lar.fields.LarTopLevelFields._
import hmda.validation.dsl.Result
import hmda.validation.rules.EditCheck
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._

object Q036 extends EditCheck[LoanApplicationRegister] {
  override def apply(lar: LoanApplicationRegister): Result = {

    val config = ConfigFactory.load()
    val loanAmount = config.getInt("hmda.validation.quality.Q036.loan.amount")

    when(lar.loan.propertyType is equalTo(2)) {
      lar.loan.amount is lessThanOrEqual(loanAmount)
    }
  }

  override def name = "Q036"

  override def fields(lar: LoanApplicationRegister) = Map(
    noField -> ""
  )

}
