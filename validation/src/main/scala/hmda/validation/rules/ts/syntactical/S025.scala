package hmda.validation.rules.ts.syntactical

import hmda.model.fi.HasControlNumber
import hmda.model.institution.Institution
import hmda.validation.context.ValidationContext
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax.PredicateOps
import hmda.validation.dsl.{ Failure, Result, Success }
import hmda.validation.rules.EditCheck

object S025 {

  def inContext[T <: HasControlNumber](ctx: ValidationContext): EditCheck[T] = {
    ctx.institution match {
      case Some(inst) => new S025(inst)
      case None => new EmptyEditCheck
    }
  }

  // this function could go away entirely, or could stay for convenience.
  def apply(input: HasControlNumber, ctx: ValidationContext): Result = {
    S025.inContext(ctx).apply(input)
  }

}

class S025[T <: HasControlNumber](institution: Institution) extends EditCheck[T] {
  def name = "S025"

  def apply(input: T): Result = compare(input.respondentId, input.agencyCode)

  private def compare(filingRespId: String, filingAgencyCode: Int): Result = {
    institution.respondentId match {
      case Left(invalid) => new Failure()
      case Right(validRespId) => {
        (filingRespId is equalTo(validRespId.id)) and (filingAgencyCode is equalTo(institution.agency.value))
      }
    }
  }
}

class EmptyEditCheck[T] extends EditCheck[T] {
  def name = "empty"
  def apply(input: T): Result = Success()
}
