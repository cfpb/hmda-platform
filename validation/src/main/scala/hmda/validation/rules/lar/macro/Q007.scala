package hmda.validation.rules.lar.`macro`

import hmda.model.fi.lar.LoanApplicationRegister
import hmda.validation.dsl.Result
import hmda.validation.rules.AggregateEditCheck
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._
import hmda.validation.rules.lar.`macro`.MacroEditTypes._
import scala.concurrent.Future

object Q007 extends AggregateEditCheck[LoanApplicationRegisterSource, LoanApplicationRegister] {

  override def name = "Q007"

  override def apply(lars: LoanApplicationRegisterSource): Future[Result] = {
    val approvedButNotAccepted =
      count(lars.filter(lar => lar.actionTakenType == 2))

    val total = count(lars)

    //TODO: make multiplier configurable
    val multiplier = 0.15

    for {
      a <- approvedButNotAccepted
      t <- total
    } yield {
      println(s"$a is less than ${t * multiplier}")
      a is lessThanOrEqual(t * multiplier)
    }

  }
}
