package hmda.validation.rules.ts.syntactical

import hmda.model.fi.lar.LoanApplicationRegister
import hmda.model.fi.ts.TransmittalSheet
import hmda.model.institution.Institution
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax.PredicateOps
import hmda.validation.dsl.{ Failure, Result }

/**
 * Created by keelerh on 7/22/16.
 */
object S025 extends { //} EditCheck[TransmittalSheet]{

  def name = "S025"

  def apply(ts: TransmittalSheet, institution: Institution): Result = {
    compare(ts.respondent.id, ts.agencyCode, institution)
  }

  def apply(lar: LoanApplicationRegister, institution: Institution): Result = {
    compare(lar.respondentId, lar.agencyCode, institution)
  }

  private def compare(filingRespId: String, filingAgencyCode: Int, institution: Institution): Result = {
    institution.respondentId match {

      // FIXME: How do I pass failure details?
      case Left(invalid) => new Failure()
      case Right(validRespId) => {
        (filingRespId is equalTo(validRespId.id)) and (filingAgencyCode is equalTo(institution.agency.value))
      }
    }
  }

}
