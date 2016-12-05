package hmda.validation.rules.ts.syntactical

import hmda.model.fi.HasControlNumber
import hmda.model.institution.Agency
import hmda.validation.dsl.Result
import hmda.validation.rules.EditCheck
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._

object S020 extends EditCheck[HasControlNumber] {

  private val agencyCodes = Agency.values.map(_.value).toSet

  override def apply(ts: HasControlNumber): Result = {
    ts.agencyCode is containedIn(agencyCodes)
  }

  override def name: String = "S020"

  override def description = ""

}
