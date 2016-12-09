package hmda.validation.rules.ts.validity

import hmda.model.fi.lar.fields.LarTopLevelFields._
import hmda.model.fi.ts.TransmittalSheet
import hmda.validation.dsl.Result
import hmda.validation.rules.EditCheck
import hmda.validation.dsl.PredicateRegEx.validZipCode
import hmda.validation.dsl.PredicateSyntax._

object V145 extends EditCheck[TransmittalSheet] {
  override def name: String = "V145"

  override def fields(lar: TransmittalSheet) = Map(
    noField -> ""
  )

  override def apply(ts: TransmittalSheet): Result = {
    ts.respondent.zipCode is validZipCode
  }
}
