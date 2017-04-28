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

object Q058 extends AggregateEditCheck[LoanApplicationRegisterSource, LoanApplicationRegister] {

  val config = ConfigFactory.load()
  val preapprovalCount = config.getInt("hmda.validation.macro.Q058.numOfPreapprovalsRequested")

  override def name = "Q058"

  override def apply[as: AS, mat: MAT, ec: EC](lars: LoanApplicationRegisterSource): Future[Result] = {

    val preapprovalRequested =
      count(lars.filter(lar => lar.preapprovals == 1))

    val preapprovalDenied =
      count(lars.filter(lar => lar.actionTakenType == 7))

    for {
      r <- preapprovalRequested
      d <- preapprovalDenied
    } yield {
      when(r is greaterThanOrEqual(preapprovalCount)) {
        d is greaterThan(0)
      }
    }

  }
}
