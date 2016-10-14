package hmda.validation.rules.lar.`macro`

import hmda.model.fi.lar.LoanApplicationRegister
import hmda.validation.dsl.Result
import hmda.validation.rules.SummaryEditCheck
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._
import hmda.validation.rules.lar.`macro`.MacroEditTypes._
import scala.concurrent.Future

object Q007 extends SummaryEditCheck[LoanApplicationRegisterSource, LoanApplicationRegister] {

  override def name = "Q007"

  override def apply(lars: LoanApplicationRegisterSource): Future[Result] = {
    val approvedButNotAccepted =
      count(lars.filter(lar => lar.actionTakenType == 2))

    val total = count(lars)

    for {
      a <- approvedButNotAccepted
      t <- total
    } yield {
      //TODO: make multiplier configurable
      a is lessThanOrEqual(t * 0.15)
    }

  }
}
