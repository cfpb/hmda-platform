package hmda.validation.rules.lar.validity

import hmda.model.fi.lar.LoanApplicationRegister
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.LarEditCheckSpec
import org.scalacheck.Gen

class V265Spec extends LarEditCheckSpec with BadValueUtils {
  property("All applications must pass") {
    forAll(larGen) { lar =>
      lar.mustPass
    }
  }

  val invalidDate: Gen[Int] = intOutsideRange(10000101, 99991231)

  property("An application with an invalid date must fail") {
    forAll(larGen, invalidDate) { (lar, date) =>
      val badLar = lar.copy(actionTakenDate = date)
      badLar.mustFail
    }
  }

  property("An application missing part of the date must fail") {
    forAll(larGen) { lar =>
      val badLar = lar.copy(actionTakenDate = 200001)
      badLar.mustFail
    }
  }

  property("An application with an invalid month must fail") {
    forAll(larGen) { lar =>
      val badLar = lar.copy(actionTakenDate = 20001301)
      badLar.mustFail
    }
  }

  property("An application with an invalid day must fail") {
    forAll(larGen) { lar =>
      val badLar = lar.copy(actionTakenDate = 20001232)
      badLar.mustFail
    }
  }

  override def check: EditCheck[LoanApplicationRegister] = V265
}
