package hmda.validation.rules.lar.quality

import com.typesafe.config.ConfigFactory
import hmda.model.fi.lar.LoanApplicationRegister
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.{ BadValueUtils, LarEditCheckSpec }
import org.scalacheck.Gen

class Q014Spec extends LarEditCheckSpec with BadValueUtils {
  val config = ConfigFactory.load()
  val max_income = config.getInt("hmda.validation.quality.Q014.applicant.max_income")

  property("passes when income is less than stated limit") {
    forAll(larGen, Gen.choose(1, max_income)) { (lar, i) =>
      val validApplicant = lar.applicant.copy(income = i.toString)
      val validLar = lar.copy(applicant = validApplicant)
      validLar.mustPass
    }
  }

  property("fails when income is too high (over configured limit)") {
    forAll(larGen, Gen.choose(max_income + 1, Int.MaxValue)) { (lar, i) =>
      val validApplicant = lar.applicant.copy(income = i.toString)
      val validLar = lar.copy(applicant = validApplicant)
      validLar.mustFail
    }
  }

  property("passes when income is not numeric") {
    forAll(larGen, Gen.alphaStr) { (lar, str) =>
      val validApplicant = lar.applicant.copy(income = str)
      val validLar = lar.copy(applicant = validApplicant)
      validLar.mustPass
    }
  }

  override def check: EditCheck[LoanApplicationRegister] = Q014
}
