package hmda.validation.rules.ts.validity

import hmda.model.fi.ts.TransmittalSheet
import hmda.validation.dsl.Result
import hmda.validation.rules.EditCheck
import hmda.validation.dsl.PredicateRegEx._
import hmda.validation.dsl.PredicateSyntax._
import hmda.validation.dsl.PredicateCommon._

/*
 Parent zip code must be in the proper format
 */
object V112 extends EditCheck[TransmittalSheet] {

  override def apply(ts: TransmittalSheet): Result = {
    (ts.parent.zipCode is validZipCode) or
      (ts.parent.zipCode.length is equalTo(0))
  }

  override def name: String = "V112"
}
