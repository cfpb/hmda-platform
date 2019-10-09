package hmda.validation.rules.lar.validity

import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.filing.lar.enums._
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._
import hmda.validation.dsl.ValidationResult
import hmda.validation.rules.EditCheck

object V638_4 extends EditCheck[LoanApplicationRegister] {
  override def name: String = "V638-4"

  override def parent: String = "V638"

  override def apply(lar: LoanApplicationRegister): ValidationResult = {
    val coAppRace = lar.coApplicant.race
    when(coAppRace.race1 is oneOf(RaceInformationNotProvided, RaceNotApplicable, RaceNoCoApplicant)) {
      (coAppRace.race2 is equalTo(EmptyRaceValue)) and
        (coAppRace.race3 is equalTo(EmptyRaceValue)) and
        (coAppRace.race4 is equalTo(EmptyRaceValue)) and
        (coAppRace.race5 is equalTo(EmptyRaceValue))
    }
  }
}
