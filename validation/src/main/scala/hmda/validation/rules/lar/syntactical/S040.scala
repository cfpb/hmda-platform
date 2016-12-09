package hmda.validation.rules.lar.syntactical

import hmda.model.fi.lar.LoanApplicationRegister
import hmda.model.fi.lar.fields.LarTopLevelFields._
import hmda.validation.dsl.{ Failure, Result, Success }
import hmda.validation.rules.EditCheck

object S040 extends EditCheck[Iterable[LoanApplicationRegister]] {

  //TODO: naive implementation, bail out as soon as a duplicate is found
  override def apply(lars: Iterable[LoanApplicationRegister]): Result = {

    val loanIds = lars.map(lar => lar.loan.id)
    val size = loanIds.size
    val uniqueIds = lars.toSeq.distinct
    val uniqueSize = uniqueIds.size
    if (size != uniqueSize) Failure() else Success()
  }

  override def name = "S040"

  override def fields(lars: Iterable[LoanApplicationRegister]) = Map(
    noField -> ""
  )

}
