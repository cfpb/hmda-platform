package hmda.validation.rules.ts.validity

import hmda.model.fi.ts.TransmittalSheet
import hmda.model.institution.Institution
import hmda.model.institution.InstitutionType._
import hmda.validation.context.ValidationContext
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateHmda._
import hmda.validation.dsl.PredicateSyntax._
import hmda.validation.dsl.Result
import hmda.validation.rules.{ EditCheck, IfInstitutionPresentIn }

class V110 private (institution: Institution) extends EditCheck[TransmittalSheet] {
  override def name: String = "V110"

  override def description = ""

  override def apply(ts: TransmittalSheet): Result = {
    when(institution.institutionType is oneOf(MBS, Affiliate)) {
      ts.parent is completeNameAndAddress
    }
  }
}

object V110 {
  def inContext(ctx: ValidationContext): EditCheck[TransmittalSheet] = {
    IfInstitutionPresentIn(ctx) { new V110(_) }
  }
}
