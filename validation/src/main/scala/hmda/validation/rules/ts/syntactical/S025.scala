package hmda.validation.rules.ts.syntactical

import hmda.model.fi.lar.LoanApplicationRegister
import hmda.model.fi.ts.TransmittalSheet
import hmda.model.institution.Institution
import hmda.validation.context.ValidationContext
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax.PredicateOps
import hmda.validation.dsl.{ Failure, Result, Success }

object S025 extends { //} EditCheck[TransmittalSheet]{

  def name = "S025"

  def apply(ts: TransmittalSheet, ctx: ValidationContext): Result = {
    compare(ts.respondent.id, ts.agencyCode, ctx)
  }

  def apply(lar: LoanApplicationRegister, ctx: ValidationContext): Result = {
    compare(lar.respondentId, lar.agencyCode, ctx)
  }

  private def compare(filingRespId: String, filingAgencyCode: Int, ctx: ValidationContext): Result = {
    ctx.institution match {

      // If institution is not present, edit is always a `Success`
      case None => Success()
      case Some(institution) => institution.respondentId match {

        // If respondentId cannot be derived, edit is always a `Failure`
        case Left(invalid) => new Failure()
        case Right(validRespId) => {
          (filingRespId is equalTo(validRespId.id)) and (filingAgencyCode is equalTo(institution.agency.value))
        }
      }
    }
  }

}
