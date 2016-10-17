package hmda.validation.rules.lar.`macro`

import hmda.model.fi.lar.LoanApplicationRegister
import hmda.validation.dsl.Result
import hmda.validation.rules.AggregateEditCheck
import hmda.validation.rules.lar.`macro`.MacroEditTypes.LoanApplicationRegisterSource
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._
import scala.concurrent.Future

object Q008 extends AggregateEditCheck[LoanApplicationRegisterSource, LoanApplicationRegister] {

  override def name = "Q008"

  override def apply(lars: LoanApplicationRegisterSource): Future[Result] = {
    val applicationWithdrawn =
      count(lars.filter(lar => lar.actionTakenType == 4))

    val total = count(lars)

    //TODO: make multiplier configurable
    val multiplier = 0.30

    for {
      a <- applicationWithdrawn
      t <- total
    } yield {
      a is lessThanOrEqual(t * multiplier)
    }

  }
}
