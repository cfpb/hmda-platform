package hmda.validation.rules.lar.validity

import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.filing.lar.enums._
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._
import hmda.validation.dsl.ValidationResult
import hmda.validation.rules.EditCheck

object V635_3 extends EditCheck[LoanApplicationRegister] {
  override def name: String = "V635-3"

  override def parent: String = "V635"

  override def apply(lar: LoanApplicationRegister): ValidationResult = {
    val appRace      = lar.applicant.race
    val invalidCodes = List(EmptyRaceValue, InvalidRaceCode)
    val races = List(appRace.race1, appRace.race2, appRace.race3, appRace.race4, appRace.race5)
      .filterNot(invalidCodes.contains(_))
    races.distinct.size is equalTo(races.size)
  }
}
