package hmda.validation.rules.lar.`macro`

import com.typesafe.config.ConfigFactory
import hmda.model.fi.lar.LoanApplicationRegister
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._
import hmda.validation.dsl.Result
import hmda.validation.rules.{ AS, AggregateEditCheck, EC, MAT }
import hmda.validation.rules.lar.`macro`.MacroEditTypes.LoanApplicationRegisterSource

import scala.concurrent.Future

object Q065 extends AggregateEditCheck[LoanApplicationRegisterSource, LoanApplicationRegister] {

  val config = ConfigFactory.load()
  val threshold = config.getInt("hmda.validation.macro.Q065.hoepaLoanLimit")

  override def name: String = "Q065"

  override def apply[as: AS, mat: MAT, ec: EC](lars: LoanApplicationRegisterSource): Future[Result] = {

    val hoepaCount = count(lars.filter(lar => lar.hoepaStatus == 1))

    for {
      h <- hoepaCount
    } yield {
      h is lessThan(threshold)
    }
  }

}
