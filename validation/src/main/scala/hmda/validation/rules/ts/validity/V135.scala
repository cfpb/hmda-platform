package hmda.validation.rules.ts.validity

import hmda.model.fi.lar.fields.LarTopLevelFields._
import hmda.model.fi.ts.TransmittalSheet
import hmda.validation.dsl.Result
import hmda.validation.rules.EditCheck
import hmda.validation.dsl.PredicateSyntax._
import hmda.validation.dsl.PredicateRegEx._

object V135 extends EditCheck[TransmittalSheet] {

  override def apply(ts: TransmittalSheet): Result = {
    ts.contact.fax is validPhoneNumber
  }

  override def name = "V135"

  override def description = ""

  override def fields(lar: TransmittalSheet) = Map(
    noField -> ""
  )
}
