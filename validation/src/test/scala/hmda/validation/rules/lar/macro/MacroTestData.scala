package hmda.validation.rules.lar.`macro`

import hmda.model.fi.lar.LoanApplicationRegister
import hmda.model.util.FITestData._
import hmda.parser.fi.lar.LarCsvParser

object MacroTestData {

  val lars: Array[LoanApplicationRegister] = {
    macroPasses.split("\n")
      .tail.map(line => LarCsvParser(line))
      .filter(x => x.isRight)
      .map(x => x.right.get)
  }

}
