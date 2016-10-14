package hmda.validation.rules.lar

import hmda.model.fi.lar.LoanApplicationRegister
import hmda.validation.dsl.{ Failure, Success }
import hmda.validation.rules.SummaryEditCheck
import org.scalatest.prop.PropertyChecks
import org.scalatest.{ MustMatchers, PropSpec }
import hmda.validation.rules.lar.`macro`.MacroEditTypes._

abstract class SummaryEditCheckSpec extends PropSpec with PropertyChecks with MustMatchers {

  import scala.concurrent.ExecutionContext.Implicits.global

  def check: SummaryEditCheck[LoanApplicationRegisterSource, LoanApplicationRegister]

  implicit class SummaryChecker(input: LoanApplicationRegisterSource) {
    def mustFail = check(input).map(x => x mustBe a[Failure])
    def mustPass = check(input).map(x => x mustBe a[Success])
  }

}
