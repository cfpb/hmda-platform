package hmda.validation.rules.syntactical.lar

import hmda.model.fi.lar.LoanApplicationRegister
import hmda.validation.dsl.{ Failure, Success, Result, CommonDsl }

object S040 extends CommonDsl {

  def apply(lars: Iterable[LoanApplicationRegister]): Result = {

    val loanIds = lars.map(lar => lar.loan.id)
    val size = loanIds.size
    val uniqueIds = lars.toSeq.distinct
    val uniqueSize = uniqueIds.size
    if (size != uniqueSize) Failure("Submission contains duplicates") else Success()
  }
}
