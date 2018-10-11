package hmda.validation.rules.lar.validity

import hmda.model.filing.lar.LarGenerators._
import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.filing.lar.enums._
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.LarEditCheckSpec

class V655_1Spec extends LarEditCheckSpec {
  override def check: EditCheck[LoanApplicationRegister] = V655_1

  property("Non-natural Applicant Cannot Have Income") {
    forAll(larGen) { (lar) =>
      lar.copy(income = "NA").mustPass

      val relevantEthnicity =
        lar.applicant.ethnicity.copy(ethnicity1 = EthnicityNotApplicable)

      val relevantRace = lar.applicant.race.copy(race1 = RaceNotApplicable)
      val relevantSex = lar.applicant.sex.copy(sexEnum = SexNotApplicable)
      val relevantLar =
        lar.copy(action = lar.action.copy(actionTakenType = LoanOriginated),
                 applicant = lar.applicant.copy(
                   ethnicity = relevantEthnicity,
                   race = relevantRace,
                   sex = relevantSex
                 ))

      relevantLar.copy(income = "NA").mustPass
      relevantLar.copy(income = "na").mustFail
      relevantLar.copy(income = "100").mustFail

      lar
        .copy(
          applicant = lar.applicant.copy(ethnicity =
            lar.applicant.ethnicity.copy(ethnicity1 = HispanicOrLatino)))
        .mustPass
      lar
        .copy(applicant = lar.applicant.copy(
          race = lar.applicant.race.copy(race1 = AmericanIndianOrAlaskaNative)))
        .mustPass
      lar
        .copy(
          applicant =
            lar.applicant.copy(sex = lar.applicant.sex.copy(sexEnum = Male)))
        .mustPass

    }
  }
}
