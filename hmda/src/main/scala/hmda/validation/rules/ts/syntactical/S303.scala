package hmda.validation.rules.ts.syntactical

import hmda.model.filing.ts.TransmittalSheet
import hmda.model.institution.Institution
import hmda.validation.context.ValidationContext
import hmda.validation.dsl.ValidationResult
import hmda.validation.rules.{ EditCheck, IfInstitutionPresentIn }
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._

object S303 {
  def withContext(ctx: ValidationContext): EditCheck[TransmittalSheet] =
    IfInstitutionPresentIn(ctx) { new S303(_) }

}

class S303 private (institution: Institution) extends EditCheck[TransmittalSheet] {
  override def name: String = "S303"

  override def apply(ts: TransmittalSheet): ValidationResult =
    (ts.LEI.toLowerCase is equalTo(institution.LEI.toLowerCase)) and
      (ts.agency.code is equalTo(institution.agency.code)) and
      (ts.taxId is equalTo(institution.taxId.getOrElse("")))
}
