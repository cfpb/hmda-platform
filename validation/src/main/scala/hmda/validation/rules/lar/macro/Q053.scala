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

object Q053 extends AggregateEditCheck[LoanApplicationRegisterSource, LoanApplicationRegister] {

  val config = ConfigFactory.load()
  val multiplier = config.getDouble("hmda.validation.macro.Q053.numOfLarsMultiplier")

  override def name = "Q053"

  override def apply[as: AS, mat: MAT, ec: EC](lars: LoanApplicationRegisterSource): Future[Result] = {

    val hoepaLoans =
      count(lars.filter(lar => lar.agencyCode == 5 && lar.actionTakenType == 1 && lar.hoepaStatus == 1))

    val originated = count(lars.filter(lar => lar.actionTakenType == 1))

    for {
      a <- hoepaLoans
      t <- originated
    } yield {
      a.toDouble is lessThanOrEqual(t * multiplier)
    }

  }
}
