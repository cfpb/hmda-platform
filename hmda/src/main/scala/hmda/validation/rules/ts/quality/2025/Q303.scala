package hmda.validation.rules.ts.quality._2025

import hmda.model.filing.ts.TransmittalSheet
import hmda.model.institution.Institution
import hmda.validation.context.ValidationContext
import hmda.validation.dsl.ValidationResult
import hmda.validation.rules.{ EditCheck, IfInstitutionPresentIn }
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._

object Q303 {
  def withContext(ctx: ValidationContext): EditCheck[TransmittalSheet] =
    IfInstitutionPresentIn(ctx) { new Q303(_) }

}

class Q303 private (institution: Institution) extends EditCheck[TransmittalSheet] {
  override def name: String = "Q303"

  override def apply(ts: TransmittalSheet): ValidationResult =
    (ts.LEI.toLowerCase is equalTo(institution.LEI.toLowerCase)) and
      (ts.agency.code is equalTo(institution.agency.code)) and
      (ts.taxId is equalTo(institution.taxId.getOrElse("")))
}
