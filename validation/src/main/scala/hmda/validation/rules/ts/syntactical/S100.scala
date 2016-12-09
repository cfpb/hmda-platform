package hmda.validation.rules.ts.syntactical

import hmda.model.fi.HasControlNumber
import hmda.model.fi.lar.fields.LarTopLevelFields._
import hmda.model.fi.ts.TransmittalSheet
import hmda.model.institution.Institution
import hmda.validation.context.ValidationContext
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax.PredicateOps
import hmda.validation.dsl.{ Failure, Result }
import hmda.validation.rules.{ EditCheck, IfInstitutionPresentIn, IfYearPresentIn }

object S100 {
  def inContext(ctx: ValidationContext): EditCheck[TransmittalSheet] = {
    IfYearPresentIn(ctx) { new S100(_) }
  }
}

class S100 private (year: Int) extends EditCheck[TransmittalSheet] {
  def name = "S100"

  override def fields(lar: TransmittalSheet) = Map(
    noField -> ""
  )

  def apply(input: TransmittalSheet): Result = input.activityYear is equalTo(year)
}
