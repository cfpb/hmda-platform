package hmda.validation.rules.lar.quality

import hmda.model.fi.lar.LoanApplicationRegister
import hmda.validation.dsl.Result
import hmda.validation.rules.EditCheck
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._
import com.typesafe.config.ConfigFactory
import hmda.model.fi.lar.fields.LarTopLevelFields._

object Q005 extends EditCheck[LoanApplicationRegister] {
  override def apply(lar: LoanApplicationRegister): Result = {

    val config = ConfigFactory.load()
    val loanAmount = config.getInt("hmda.validation.quality.Q005.loan.amount")

    when((lar.purchaserType is oneOf(1, 2, 3, 4)) and
      (lar.loan.propertyType is oneOf(1, 2))) {
      lar.loan.amount is lessThanOrEqual(loanAmount)
    }
  }

  override def name = "Q005"

}
