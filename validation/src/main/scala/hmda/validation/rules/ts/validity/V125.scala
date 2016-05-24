package hmda.validation.rules.ts.validity

import hmda.model.fi.ts.TransmittalSheet
import hmda.validation.dsl.Result
import hmda.validation.rules.EditCheck
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._
import hmda.validation.dsl.PredicateRegEx._

/*
 Tax ID must be properly formatted
 */
object V125 extends EditCheck[TransmittalSheet] {

  override def apply(ts: TransmittalSheet): Result = {
    (ts.taxId is validTaxId) and
      (ts.taxId not equalTo("99-9999999")) and
      (ts.taxId not equalTo("00-0000000"))
  }

  override def name: String = "V125"
}
