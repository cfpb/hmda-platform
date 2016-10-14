package hmda.validation.rules.lar.`macro`

import akka.stream.scaladsl.Source
import hmda.model.fi.lar.LoanApplicationRegister
import hmda.model.util.FITestData._
import hmda.parser.fi.lar.LarCsvParser
import hmda.validation.rules.lar.SummaryEditCheckSpec
import hmda.validation.rules.lar.`macro`.MacroEditTypes._

abstract class MacroSpec extends SummaryEditCheckSpec {

  val lars: Array[LoanApplicationRegister] = {
    fiCSV.split("\n")
      .tail.map(line => LarCsvParser(line))
      .filter(x => x.isRight)
      .map(x => x.right.get)
  }

  val larSource: LoanApplicationRegisterSource = {
    Source.fromIterator(() => lars.toIterator)
  }

}
