package hmda.validation.rules.lar.`macro`

import hmda.validation._
import com.typesafe.config.ConfigFactory
import hmda.model.fi.lar.LoanApplicationRegister
import hmda.validation.dsl.Result
import hmda.validation.rules.AggregateEditCheck
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._
import hmda.validation.rules.lar.`macro`.MacroEditTypes._

import scala.concurrent.Future

object Q048 extends AggregateEditCheck[LoanApplicationRegisterSource, LoanApplicationRegister] {

  val config = ConfigFactory.load()
  val multiplier = config.getDouble("hmda.validation.macro.Q048.numOfLarsMultiplier")

  override def name = "Q048"

  override def apply[as: AS, mat: MAT, ec: EC](lars: LoanApplicationRegisterSource): Future[Result] = {

    val incompletePreapprovalRequest =
      count(lars.filter(lar => lar.preapprovals == 1 && lar.actionTakenType == 5))

    val total = count(lars)

    for {
      i <- incompletePreapprovalRequest
      t <- total
    } yield {
      i.toDouble is lessThanOrEqual(t * multiplier)
    }

  }
}
