package hmda.validation.rules.lar.`macro`

import akka.NotUsed
import akka.stream.scaladsl.Source
import hmda.model.fi.lar.LoanApplicationRegister

object MacroEditTypes {
  type LoanApplicationRegisterSource = Source[LoanApplicationRegister, NotUsed]
}
