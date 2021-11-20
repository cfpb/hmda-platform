package hmda.validation.rules.lar.syntactical

import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.filing.ts.TransmittalSheet
import hmda.util.QuarterTimeBarrier
import hmda.utils.YearUtils._
import hmda.validation.context.ValidationContext
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._
import hmda.validation.dsl.{ValidationFailure, ValidationResult, ValidationSuccess}
import hmda.validation.rules.{EditCheck, IfTsPresentIn}

object S307 {
  def withContext(ctx: ValidationContext): EditCheck[LoanApplicationRegister] =
    IfTsPresentIn(ctx) { new S307(_) }

}

class S307 private (ts: TransmittalSheet) extends EditCheck[LoanApplicationRegister] {
  override def name: String = "S307"

  override def apply(lar: LoanApplicationRegister): ValidationResult = {

    if (isValidQuarterTS(ts.quarter)){

    val actionTakenDateInRange= QuarterTimeBarrier.actionTakenInQuarterRange(lar.action.actionTakenDate,Period(ts.year,Some(ts.quarter.toString)))

      if(actionTakenDateInRange){
        ValidationSuccess
      }else {
        ValidationFailure
      }
    }
  }
}
