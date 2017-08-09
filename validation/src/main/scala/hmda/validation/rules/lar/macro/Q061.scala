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

object Q061 extends AggregateEditCheck[LoanApplicationRegisterSource, LoanApplicationRegister] {

  val config = ConfigFactory.load()
  val multiplier = config.getDouble("hmda.validation.macro.Q061.numOfLarsMultiplier")

  override def name = "Q061"

  override def apply[as: AS, mat: MAT, ec: EC](lars: LoanApplicationRegisterSource): Future[Result] = {

    val firstLienPropertyLoans =
      count(lars.filter(lar => lar.loan.propertyType == 1 && lar.actionTakenType == 1 && lar.lienStatus == 1 && lar.rateSpread != "NA")
        .filter(lar => lar.rateSpread.toDouble > 5.0))

    val total = count(lars.filter(lar => lar.actionTakenType == 1))

    for {
      f <- firstLienPropertyLoans
      t <- total
    } yield {
      f.toDouble is lessThanOrEqual(t * multiplier)
    }

  }
}
