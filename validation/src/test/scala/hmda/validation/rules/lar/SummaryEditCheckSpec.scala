package hmda.validation.rules.lar

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import hmda.model.fi.lar.LoanApplicationRegister
import hmda.validation.dsl.{ Failure, Success }
import hmda.validation.rules.AggregateEditCheck
import org.scalatest._
import hmda.validation.rules.lar.`macro`.MacroEditTypes._

import scala.concurrent.Future

abstract class SummaryEditCheckSpec extends AsyncWordSpec with MustMatchers {

  implicit val system: ActorSystem
  implicit val materializer: ActorMaterializer

  def check: AggregateEditCheck[LoanApplicationRegisterSource, LoanApplicationRegister]

  implicit class SummaryChecker(input: LoanApplicationRegisterSource) {
    def mustFail: Future[Assertion] = check(input).map(x => x mustBe a[Failure])
    def mustPass: Future[Assertion] = check(input).map(x => x mustBe a[Success])
  }

}
