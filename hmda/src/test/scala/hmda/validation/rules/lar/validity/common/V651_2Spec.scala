package hmda.validation.rules.lar.validity

import hmda.model.filing.lar.LarGenerators._
import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.filing.lar.enums._
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.LarEditCheckSpec

class V651_2Spec extends LarEditCheckSpec {
  override def check: EditCheck[LoanApplicationRegister] = V651_2

  property("If applicant isn't real, age must equal 8888") {
    forAll(larGen) { lar =>
      val unapplicableLar1 = lar.copy(
        applicant = lar.applicant.copy(
          race = lar.applicant.race.copy(race1 = new InvalidRaceCode)))
      unapplicableLar1.mustPass
      val unapplicableLar2 =
        lar.copy(action = lar.action.copy(actionTakenType = PurchasedLoan))
      unapplicableLar2.mustPass

      val ethnicityNA =
        lar.applicant.ethnicity.copy(ethnicity1 = EthnicityNotApplicable)
      val raceNA = lar.applicant.race.copy(race1 = RaceNotApplicable)
      val sexNA = lar.applicant.sex.copy(sexEnum = SexNotApplicable)
      val applicableLar =
        lar.copy(applicant = lar.applicant.copy(race = raceNA,
                                                ethnicity = ethnicityNA,
                                                sex = sexNA),
                 action = lar.action.copy(actionTakenType = LoanOriginated))
      applicableLar.mustFail

      val validLar =
        applicableLar.copy(applicant = applicableLar.applicant.copy(age = 8888))
      validLar.mustPass
    }
  }
}
