package hmda.validation.rules.lar.validity

import hmda.validation.rules.lar.validity.common.V619_2
import hmda.model.filing.lar.LarGenerators._
import hmda.model.filing.lar.LoanApplicationRegister
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.LarEditCheckSpec

class V619_2SpecWithYear(actionYear: String) extends LarEditCheckSpec {
  override def check: EditCheck[LoanApplicationRegister] = V619_2.withYear(actionYear)

  property("Action taken date must be in the current year") {
    forAll(larGen) { lar =>
      val year = actionYear.toInt
      val badActionYear = (year - 1) * 1000 + 110
      val goodActionYear = year * 10000 + 101

      val badActionDate1 =
        lar.action.copy(actionTakenDate = 0)
      val badActionDate2 = lar.action.copy(actionTakenDate = badActionYear)
      lar.copy(action = badActionDate1).mustFail
      lar.copy(action = badActionDate2).mustFail

      val goodAction = lar.action.copy(actionTakenDate = goodActionYear)
      lar.copy(action = goodAction).mustPass
    }
  }
}

class y2018V619_2Spec extends V619_2SpecWithYear("2018")
class y2019V619_2Spec extends V619_2SpecWithYear("2019")
class y2020V619_2Spec extends V619_2SpecWithYear("2020")
class y2021V619_2Spec extends V619_2SpecWithYear("2021")
class y2022V619_2Spec extends V619_2SpecWithYear("2022")
class y2023V619_2Spec extends V619_2SpecWithYear("2023")
class y2024V619_2Spec extends V619_2SpecWithYear("2024")
class y2025V619_2Spec extends V619_2SpecWithYear("2025")
