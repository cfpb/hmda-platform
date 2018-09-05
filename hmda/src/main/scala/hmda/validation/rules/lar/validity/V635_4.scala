package hmda.validation.rules.lar.validity

import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.filing.lar.enums._
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._
import hmda.validation.dsl.ValidationResult
import hmda.validation.rules.EditCheck

object V635_4 extends EditCheck[LoanApplicationRegister] {
  override def name: String = "V635-4"

  override def parent: String = "V635"

  override def apply(lar: LoanApplicationRegister): ValidationResult = {
    val appRace = lar.applicant.race
    when(appRace.race1 is oneOf(RaceInformationNotProvided, RaceNotApplicable)) {
      (appRace.race2 is equalTo(EmptyRaceValue)) and
        (appRace.race3 is equalTo(EmptyRaceValue)) and
        (appRace.race4 is equalTo(EmptyRaceValue)) and
        (appRace.race5 is equalTo(EmptyRaceValue))
    }
  }
}
