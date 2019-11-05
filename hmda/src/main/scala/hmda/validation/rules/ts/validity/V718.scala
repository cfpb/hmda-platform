package hmda.validation.rules.ts.validity

import hmda.model.filing.ts.TransmittalSheet
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._
import hmda.validation.dsl.ValidationResult
import hmda.validation.rules.EditCheck
import hmda.validation.context.ValidationContext

object V718 {
  def withContext(validationContext: ValidationContext): EditCheck[TransmittalSheet] =
    new V718(validationContext)
}

class V718 private (validationContext: ValidationContext) extends EditCheck[TransmittalSheet] {
  override def name: String = "V718"

  val quarter = 
    validationContext.filingPeriod match {
      case Some(period) => period.quarter match {
        case Some("Q1") => 1
        case Some("Q2") => 2
        case Some("Q3") => 3
        case _ => -1
      }
      case None => -1
    }

  override def apply(ts: TransmittalSheet): ValidationResult =
    ts.quarter is equalTo(quarter)
}
