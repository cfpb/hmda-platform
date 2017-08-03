package hmda.validation.rules.lar

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import hmda.model.fi.lar.LoanApplicationRegister
import hmda.validation.dsl.{ Failure, Success }
import hmda.validation.rules.AggregateEditCheck
import hmda.validation.rules.lar.`macro`.MacroEditTypes._
import scala.concurrent._
import scala.concurrent.duration._
import org.scalatest._
import org.scalatest.prop.PropertyChecks

import scala.concurrent.ExecutionContext

abstract class SummaryEditCheckSpec extends PropSpec with PropertyChecks with MustMatchers {

  implicit val system: ActorSystem
  implicit val materializer: ActorMaterializer
  implicit val timeout = 5.seconds

  def check: AggregateEditCheck[LoanApplicationRegisterSource, LoanApplicationRegister]

  implicit class SummaryChecker(input: LoanApplicationRegisterSource)(implicit ec: ExecutionContext) {
    def mustFail: Assertion = {
      val fResult = check(input).map(x => x mustBe a[Failure])
      Await.result(fResult, timeout)
    }
    def mustPass: Assertion = {
      val fResult = check(input).map(x => x mustBe a[Success])
      Await.result(fResult, timeout)
    }
  }

}
