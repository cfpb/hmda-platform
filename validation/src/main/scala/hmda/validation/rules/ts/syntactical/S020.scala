package hmda.validation.rules.ts.syntactical

import hmda.model.fi.ts.TransmittalSheet
import hmda.validation.dsl.Result
import hmda.validation.rules.EditCheck

object S020 extends EditCheck[TransmittalSheet] {

  import hmda.validation.dsl.PredicateDefaults._
  import hmda.validation.dsl.PredicateSyntax._

  //Hardcoded for now
  val agencyCodes: List[Int] = List(1, 2, 3, 5, 7, 9)

  def apply(ts: TransmittalSheet): Result = {
    ts.agencyCode is containedIn(agencyCodes)
  }

  override def name: String = "S020"

}
