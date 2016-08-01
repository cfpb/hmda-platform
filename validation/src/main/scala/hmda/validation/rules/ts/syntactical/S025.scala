package hmda.validation.rules.ts.syntactical

import hmda.model.fi.lar.LoanApplicationRegister
import hmda.model.fi.ts.TransmittalSheet
import hmda.model.institution.Institution
import hmda.validation.context.ValidationContext
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax.PredicateOps
import hmda.validation.dsl.{ Failure, Result, Success }
import hmda.validation.rules.EditCheck

object S025 {

  def inContext(ctx: ValidationContext) = {
    ctx.institution match {
      case Some(inst) => new S025(inst)
      case None => new EmptyEditCheck
    }
  }

  def name = "S025" // this would go away too

  // TODO the necessary things to make an EditCheck[T <: HasControlId] or whatever.
  // then soon this function could go away entirely.
  def apply(ts: TransmittalSheet, ctx: ValidationContext): Result = {
    new S025(ctx.institution.get).apply(ts)
  }

}

class S025(institution: Institution) extends EditCheck[LoanApplicationRegister] {
  def name = "S025"

  def apply(ts: TransmittalSheet): Result = {
    compare(ts.respondent.id, ts.agencyCode)
  }

  def apply(lar: LoanApplicationRegister): Result = {
    compare(lar.respondentId, lar.agencyCode)
  }

  private def compare(filingRespId: String, filingAgencyCode: Int): Result = {
    institution.respondentId match {
      case Left(invalid) => new Failure()
      case Right(validRespId) => {
        (filingRespId is equalTo(validRespId.id)) and (filingAgencyCode is equalTo(institution.agency.value))
      }
    }
  }
}

class EmptyEditCheck extends EditCheck[LoanApplicationRegister] {
  def name = "empty"
  def apply(lar: LoanApplicationRegister): Result = Success()
  def apply(transmittalSheet: TransmittalSheet): Result = Success()
}
