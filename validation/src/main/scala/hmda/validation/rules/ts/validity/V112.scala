package hmda.validation.rules.ts.validity

import hmda.model.fi.ts.TransmittalSheet
import hmda.validation.dsl.Result
import hmda.validation.rules.EditCheck
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._

/*
 Parent zip code must be in the proper format
 */
object V112 extends EditCheck[TransmittalSheet] {

  override def apply(ts: TransmittalSheet): Result = {
    val regex = "^(?:\\d{5}(?:-\\d{4})?)?$".r // Matches "", "12345", or "12345-6789"

    regex.findFirstIn(ts.parent.zipCode).isEmpty is equalTo(false)
  }

  override def name: String = "V112"
}
