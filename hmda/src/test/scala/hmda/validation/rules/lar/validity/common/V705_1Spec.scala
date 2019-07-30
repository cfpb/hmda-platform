package hmda.validation.rules.lar.validity

import hmda.model.filing.lar.LarGenerators.larGen
import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.filing.lar.enums._
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.LarEditCheckSpec

class V705_1Spec extends LarEditCheckSpec {
  override def check: EditCheck[LoanApplicationRegister] = V705_1
  property(
    "If applicant is a non-natural person and there is no co-applicant, AUS must be exempt or NA") {
    forAll(larGen) { lar =>
      val unappLar = lar.copy(
        applicant = lar.applicant.copy(ethnicity =
          lar.applicant.ethnicity.copy(ethnicity1 = EmptyEthnicityValue)))
      unappLar.mustPass

      val appApplicant = lar.applicant.copy(
        ethnicity =
          lar.applicant.ethnicity.copy(ethnicity1 = EthnicityNotApplicable),
        race = lar.applicant.race.copy(race1 = RaceNotApplicable),
        sex = lar.applicant.sex.copy(sexEnum = SexNotApplicable)
      )
      val appCoApplicant = lar.coApplicant.copy(
        ethnicity =
          lar.coApplicant.ethnicity.copy(ethnicity1 = EthnicityNoCoApplicant),
        race = lar.coApplicant.race.copy(race1 = RaceNoCoApplicant),
        sex = lar.coApplicant.sex.copy(sexEnum = SexNoCoApplicant)
      )
      val appLar =
        lar.copy(applicant = appApplicant, coApplicant = appCoApplicant)

      appLar.copy(AUS = appLar.AUS.copy(aus1 = OtherAUS)).mustFail
      appLar
        .copy(ausResult =
          appLar.ausResult.copy(ausResult1 = OtherAutomatedUnderwritingResult))
        .mustFail

      appLar
        .copy(AUS = appLar.AUS.copy(aus1 = AUSNotApplicable),
              ausResult = appLar.ausResult.copy(ausResult1 = AUSResultExempt))
        .mustPass
    }
  }
}
