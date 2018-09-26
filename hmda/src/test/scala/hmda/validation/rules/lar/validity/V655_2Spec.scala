package hmda.validation.rules.lar.validity

import hmda.model.filing.lar.LarGenerators._
import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.filing.lar.enums._
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.LarEditCheckSpec

class V655_2Spec extends LarEditCheckSpec {
  override def check: EditCheck[LoanApplicationRegister] = V655_2

  property("Non-natural Co-Applicant Cannot Have Income") {
    forAll(larGen) { (lar) =>
      lar.copy(income = "NA").mustPass
      val relevantEthnicity =
        lar.coApplicant.ethnicity.copy(ethnicity1 = EthnicityNotApplicable)
      val relevantRace = lar.coApplicant.race.copy(race1 = RaceNotApplicable)
      val relevantSex = lar.coApplicant.sex.copy(sexEnum = SexNotApplicable)
      val relevantLar = lar.copy(
        action = lar.action.copy(actionTakenType = LoanOriginated),
        coApplicant = lar.coApplicant.copy(
          ethnicity = relevantEthnicity,
          race = relevantRace,
          sex = relevantSex
        )
      )

      relevantLar.copy(income = "NA").mustPass
      relevantLar.copy(income = "na").mustFail
      relevantLar.copy(income = "100").mustFail

      lar
        .copy(
          coApplicant = lar.coApplicant.copy(ethnicity =
            lar.coApplicant.ethnicity.copy(ethnicity1 = HispanicOrLatino)))
        .mustPass
      lar
        .copy(
          coApplicant = lar.coApplicant.copy(race =
            lar.coApplicant.race.copy(race1 = AmericanIndianOrAlaskaNative)))
        .mustPass
      lar
        .copy(coApplicant =
          lar.coApplicant.copy(sex = lar.coApplicant.sex.copy(sexEnum = Male)))
        .mustPass

    }
  }
}
