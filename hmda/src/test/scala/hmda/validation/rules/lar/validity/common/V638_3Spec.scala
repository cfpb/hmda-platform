package hmda.validation.rules.lar.validity

import hmda.model.filing.lar.LarGenerators._
import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.filing.lar.enums._
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.LarEditCheckSpec

class V638_3Spec extends LarEditCheckSpec {
  override def check: EditCheck[LoanApplicationRegister] = V638_3

  property("Race codes cannot be repeated") {
    forAll(larGen) { lar =>
      val validRace1 = lar.coApplicant.race.copy(
        race1 = EmptyRaceValue,
        race2 = EmptyRaceValue,
        race3 = EmptyRaceValue,
        race4 = EmptyRaceValue,
        race5 = EmptyRaceValue
      )

      val validRace2 =
        lar.coApplicant.race.copy(race1 = AmericanIndianOrAlaskaNative,
                                  race2 = Asian,
                                  race3 = AsianIndian,
                                  race4 = Chinese,
                                  race5 = Filipino)

      val invalidRace =
        lar.coApplicant.race.copy(race2 = EmptyRaceValue,
                                  race3 = AmericanIndianOrAlaskaNative,
                                  race4 = AmericanIndianOrAlaskaNative,
                                  race5 = EmptyRaceValue)

      val validLar1 =
        lar.copy(coApplicant = lar.coApplicant.copy(race = validRace1))
      validLar1.mustPass

      val validLar2 =
        lar.copy(coApplicant = lar.coApplicant.copy(race = validRace2))
      validLar2.mustPass

      val invalidLar =
        lar.copy(coApplicant = lar.coApplicant.copy(race = invalidRace))
      invalidLar.mustFail

    }
  }
}
