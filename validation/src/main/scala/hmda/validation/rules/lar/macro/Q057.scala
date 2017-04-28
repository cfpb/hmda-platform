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

object Q057 extends AggregateEditCheck[LoanApplicationRegisterSource, LoanApplicationRegister] {

  val config = ConfigFactory.load()
  val larCount = config.getInt("hmda.validation.macro.Q057.numOfLoanApplications")

  override def name = "Q057"

  override def apply[as: AS, mat: MAT, ec: EC](lars: LoanApplicationRegisterSource): Future[Result] = {

    val denied =
      count(lars.filter(lar => lar.actionTakenType == 3))

    val total = count(lars)

    for {
      d <- denied
      t <- total
    } yield {
      when(t is greaterThanOrEqual(larCount)) {
        d is greaterThan(0)
      }
    }

  }
}
