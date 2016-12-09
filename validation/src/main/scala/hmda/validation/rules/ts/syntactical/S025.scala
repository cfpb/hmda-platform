package hmda.validation.rules.ts.syntactical

import hmda.model.fi.HasControlNumber
import hmda.model.fi.lar.fields.LarTopLevelFields._
import hmda.model.fi.ts.TransmittalSheet
import hmda.model.institution.Institution
import hmda.validation.context.ValidationContext
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax.PredicateOps
import hmda.validation.dsl.{ Failure, Result }
import hmda.validation.rules.{ EditCheck, IfInstitutionPresentIn }

object S025 {
  def inContext(ctx: ValidationContext): EditCheck[HasControlNumber] = {
    IfInstitutionPresentIn(ctx) { new S025(_) }
  }
}

class S025 private (institution: Institution) extends EditCheck[HasControlNumber] {
  def name = "S025"

  def apply(input: HasControlNumber): Result = compare(input.respondentId, input.agencyCode)

  override def fields(lar: HasControlNumber) = Map(
    noField -> ""
  )

  private def compare(filingRespId: String, filingAgencyCode: Int): Result = {
    institution.respondentId match {
      case Left(invalid) => Failure()
      case Right(validRespId) =>
        (filingRespId is equalTo(validRespId.id)) and (filingAgencyCode is equalTo(institution.agency.value))
    }
  }
}
