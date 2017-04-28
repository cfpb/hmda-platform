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

object Q054 extends AggregateEditCheck[LoanApplicationRegisterSource, LoanApplicationRegister] {

  val config = ConfigFactory.load()
  val multiplier = config.getDouble("hmda.validation.macro.Q054.numOfLarsMultiplier")

  override def name = "Q054"

  override def apply[as: AS, mat: MAT, ec: EC](lars: LoanApplicationRegisterSource): Future[Result] = {

    val hoepaLoans =
      count(lars.filter(lar => lar.agencyCode == 5 && lar.actionTakenType == 6 && lar.hoepaStatus == 1))

    val purchased = count(lars.filter(lar => lar.actionTakenType == 6))

    for {
      a <- hoepaLoans
      t <- purchased
    } yield {
      a.toDouble is lessThanOrEqual(t * multiplier)
    }

  }
}
