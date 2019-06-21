package hmda.validation.rules.ts.syntactical._2019

import hmda.model.filing.ts._2019.TransmittalSheet
import hmda.validation.context.ValidationContext
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._
import hmda.validation.dsl.ValidationResult
import hmda.validation.rules.{EditCheck, IfYearPresentIn}

object S302 {
  def withContext(ctx: ValidationContext): EditCheck[TransmittalSheet] = {
    IfYearPresentIn(ctx) { new S302(_) }
  }

}

class S302 private (year: Int) extends EditCheck[TransmittalSheet] {
  override def name: String = "S302"

  override def apply(ts: TransmittalSheet): ValidationResult = {
    ts.year is equalTo(year)
  }
}
