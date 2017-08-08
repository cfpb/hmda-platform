package hmda.validation.rules.ts.syntactical

import hmda.model.fi.ts.TransmittalSheet
import hmda.validation.context.ValidationContext
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax.PredicateOps
import hmda.validation.dsl.Result
import hmda.validation.rules.{ EditCheck, IfYearPresentIn }

object S100 {
  def inContext(ctx: ValidationContext): EditCheck[TransmittalSheet] = {
    IfYearPresentIn(ctx) { new S100(_) }
  }
}

class S100 private (year: Int) extends EditCheck[TransmittalSheet] {
  def name = "S100"

  def apply(input: TransmittalSheet): Result = input.activityYear is equalTo(year)
}
