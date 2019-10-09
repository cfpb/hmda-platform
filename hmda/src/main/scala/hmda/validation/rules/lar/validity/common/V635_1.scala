package hmda.validation.rules.lar.validity

import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.filing.lar.enums._
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._
import hmda.validation.dsl.ValidationResult
import hmda.validation.rules.EditCheck

object V635_1 extends EditCheck[LoanApplicationRegister] {
  override def name: String = "V635-1"

  override def parent: String = "V635"

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
    RaceNotApplicable
  )

  override def apply(lar: LoanApplicationRegister): ValidationResult =
    when(
      (lar.applicant.race.otherAsianRace is empty) and
        (lar.applicant.race.otherPacificIslanderRace is empty) and
        (lar.applicant.race.otherNativeRace is empty)
    ) {
      lar.applicant.race.race1 is containedIn(validRaceValues)
    }
}
