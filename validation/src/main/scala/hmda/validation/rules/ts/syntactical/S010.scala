package hmda.validation.rules.ts.syntactical

import hmda.model.fi.ts.TransmittalSheet
import hmda.validation.dsl.Result
import hmda.validation.rules.EditCheck
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._

/*
 The first record in the file must = 1 (TS)
 */

object S010 extends EditCheck[TransmittalSheet] {

  //Hardcoded for now
  val tsId = 1

  override def apply(ts: TransmittalSheet): Result = {
    ts.id is equalTo(tsId)
  }

  override def name: String = "S010"
}
