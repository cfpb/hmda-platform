package hmda.validation.rules.ts.syntactical

import hmda.model.fi.ts.TransmittalSheet
import hmda.validation.dsl.Result
import hmda.validation.rules.EditCheck
import hmda.validation.dsl.PredicateDefaults._
import hmda.validation.dsl.PredicateSyntax._

object S020 extends EditCheck[TransmittalSheet] {

  //Hardcoded for now
  val agencyCodes: List[Int] = List(1, 2, 3, 5, 7, 9)

  override def apply(ts: TransmittalSheet): Result = {
    ts.agencyCode is containedIn(agencyCodes)
  }

  override def name: String = "S020"

}
