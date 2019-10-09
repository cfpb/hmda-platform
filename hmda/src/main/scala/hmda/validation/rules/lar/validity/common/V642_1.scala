package hmda.validation.rules.lar.validity

import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.filing.lar.enums._
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._
import hmda.validation.dsl.ValidationResult
import hmda.validation.rules.EditCheck

object V642_1 extends EditCheck[LoanApplicationRegister] {
  override def name: String = "V642-1"

  override def parent: String = "V642"

  val validSexValues = List(Male, Female, SexInformationNotProvided, SexNotApplicable, MaleAndFemale)

  override def apply(lar: LoanApplicationRegister): ValidationResult =
    lar.applicant.sex.sexEnum is containedIn(validSexValues)
}
