package hmda.validation.rules.lar.validity

import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.filing.lar.enums._
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._
import hmda.validation.dsl.ValidationResult
import hmda.validation.rules.EditCheck

object V638_2 extends EditCheck[LoanApplicationRegister] {
  override def name: String = "V638-2"

  override def parent: String = "V638"

  val validRaceValues = List(
    EmptyRaceValue,
    AmericanIndianOrAlaskaNative,
    Asian,
    AsianIndian,
    Chinese,
    Filipino,
    Japanese,
    Korean,
    Vietnamese,
    OtherAsian,
    BlackOrAfricanAmerican,
    NativeHawaiianOrOtherPacificIslander,
    NativeHawaiian,
    GuamanianOrChamorro,
    Samoan,
    OtherPacificIslander,
    White
  )

  override def apply(lar: LoanApplicationRegister): ValidationResult =
    (lar.coApplicant.race.race2 is containedIn(validRaceValues)) and
      (lar.coApplicant.race.race3 is containedIn(validRaceValues)) and
      (lar.coApplicant.race.race4 is containedIn(validRaceValues)) and
      (lar.coApplicant.race.race5 is containedIn(validRaceValues))
}
