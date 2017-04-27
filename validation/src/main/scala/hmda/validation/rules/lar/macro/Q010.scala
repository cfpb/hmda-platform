package hmda.validation.rules.lar.`macro`

import hmda.validation._
import com.typesafe.config.ConfigFactory
import hmda.model.fi.lar.LoanApplicationRegister
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._
import hmda.validation.dsl.Result
import hmda.validation.rules.AggregateEditCheck
import hmda.validation.rules.lar.`macro`.MacroEditTypes._

import scala.concurrent.Future

object Q010 extends AggregateEditCheck[LoanApplicationRegisterSource, LoanApplicationRegister] {

  val config = ConfigFactory.load()
  val multiplier = config.getDouble("hmda.validation.macro.Q010.numOfLarsMultiplier")

  override def name = "Q010"

  override def apply[as: AS, mat: MAT, ec: EC](lars: LoanApplicationRegisterSource): Future[Result] = {

    val originated = count(lars.filter(lar => lar.actionTakenType == 1))

    val nonPreapprovals = count(lars.filter(lar => (1 to 6).contains(lar.actionTakenType)))

    for {
      a <- originated
      t <- nonPreapprovals
    } yield {
      a.toDouble is greaterThanOrEqual(t * multiplier)
    }

  }
}
