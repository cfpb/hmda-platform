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

object Q047 extends AggregateEditCheck[LoanApplicationRegisterSource, LoanApplicationRegister] {

  val config = ConfigFactory.load()
  val multiplier = config.getDouble("hmda.validation.macro.Q047.numOfLarsMultiplier")

  override def name = "Q047"

  override def apply[as: AS, mat: MAT, ec: EC](lars: LoanApplicationRegisterSource): Future[Result] = {

    val singleFamilyWithdrawn =
      count(lars.filter(lar => lar.actionTakenType == 4 && lar.preapprovals == 1))

    val total = count(lars)

    for {
      a <- singleFamilyWithdrawn
      t <- total
    } yield {
      a.toDouble is lessThanOrEqual(t * multiplier)
    }

  }
}
