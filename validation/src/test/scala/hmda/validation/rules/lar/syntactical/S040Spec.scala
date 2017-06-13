package hmda.validation.rules.lar.syntactical

import akka.stream.scaladsl.Source
import hmda.model.fi.lar.LoanApplicationRegister
import hmda.validation.rules.AggregateEditCheck
import hmda.validation.rules.lar.`macro`.MacroEditTypes.LoanApplicationRegisterSource
import hmda.validation.rules.lar.`macro`.MacroSpec

class S040Spec extends MacroSpec {
  override def check: AggregateEditCheck[LoanApplicationRegisterSource, LoanApplicationRegister] = S040

  property("fails if any loan ID is repeated") {
    val newLoan = sampleLar.loan.copy(id = "duplicateId")
    val newCollection = sampleLar.copy(loan = newLoan) +: lars :+ sampleLar.copy(loan = newLoan)
    val newSource = Source.fromIterator(() => newCollection.toIterator)

    newSource.mustFail
  }

  property("passes if all lars have different loan IDs") {
    larSource.mustPass
  }

}
