package hmda.validation.rules.lar.validity

import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.filing.lar.enums._
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._
import hmda.validation.dsl.ValidationResult
import hmda.validation.rules.EditCheck

object V638_1 extends EditCheck[LoanApplicationRegister] {
  override def name: String = "V638-1"

  override def parent: String = "V638"

  val validRaceValues = List(
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
    White,
    RaceInformationNotProvided,
    RaceNotApplicable,
    RaceNoCoApplicant
  )

  override def apply(lar: LoanApplicationRegister): ValidationResult =
    when(
      (lar.coApplicant.race.otherAsianRace is empty) and
        (lar.coApplicant.race.otherPacificIslanderRace is empty) and
        (lar.coApplicant.race.otherNativeRace is empty)
    ) {
      lar.coApplicant.race.race1 is containedIn(validRaceValues)
    }
}
