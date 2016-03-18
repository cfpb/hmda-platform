package hmda.validation.rules.lar.syntactical

import hmda.model.fi.lar.LoanApplicationRegister
import hmda.validation.dsl.{ CommonDsl, Failure, Result, Success }

object S040 extends CommonDsl {

  //TODO: naive implementation, bail out as soon as a duplicate is found
  def apply(lars: Iterable[LoanApplicationRegister]): Result = {

    val loanIds = lars.map(lar => lar.loan.id)
    val size = loanIds.size
    val uniqueIds = lars.toSeq.distinct
    val uniqueSize = uniqueIds.size
    if (size != uniqueSize) Failure("Submission contains duplicates") else Success()
  }
}
