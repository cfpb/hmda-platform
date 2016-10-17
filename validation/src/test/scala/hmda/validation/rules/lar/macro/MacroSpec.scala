package hmda.validation.rules.lar.`macro`

import akka.stream.scaladsl.Source
import hmda.validation.rules.lar.SummaryEditCheckSpec
import hmda.validation.rules.lar.`macro`.MacroEditTypes._

abstract class MacroSpec extends SummaryEditCheckSpec {

  val lars = MacroTestData.lars

  val larSource: LoanApplicationRegisterSource = {
    Source.fromIterator(() => lars.toIterator)
  }

}
