package hmda.validation.rules.lar.validity

import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.filing.lar.enums._
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._
import hmda.validation.dsl.ValidationResult
import hmda.validation.rules.EditCheck

object V638_3 extends EditCheck[LoanApplicationRegister] {
  override def name: String = "V638-3"

  override def parent: String = "V638"

  override def apply(lar: LoanApplicationRegister): ValidationResult = {
    val coAppRace    = lar.coApplicant.race
    val invalidCodes = List(EmptyRaceValue, InvalidRaceCode)
    val races = List(coAppRace.race1, coAppRace.race2, coAppRace.race3, coAppRace.race4, coAppRace.race5)
      .filterNot(invalidCodes.contains(_))
    races.distinct.size is equalTo(races.size)
  }
}
