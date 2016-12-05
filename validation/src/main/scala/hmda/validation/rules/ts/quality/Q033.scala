package hmda.validation.rules.ts.quality

import hmda.model.fi.ts.TransmittalSheet
import hmda.model.institution.Institution
import hmda.model.institution.InstitutionType._
import hmda.validation.context.ValidationContext
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateHmda._
import hmda.validation.dsl.PredicateSyntax._
import hmda.validation.dsl.Result
import hmda.validation.rules.{ EditCheck, IfInstitutionPresentIn }

class Q033 private (respondent: Institution) extends EditCheck[TransmittalSheet] {
  override def name: String = "Q033"

  override def description = ""

  override def apply(ts: TransmittalSheet): Result = {
    when((respondent.institutionType is oneOf(Bank, SavingsAndLoan, IndependentMortgageCompany))
      and (respondent.hasParent is true)) {
      ts.parent is completeNameAndAddress
    }
  }
}

object Q033 {
  def inContext(ctx: ValidationContext): EditCheck[TransmittalSheet] = {
    IfInstitutionPresentIn(ctx) { new Q033(_) }
  }
}
