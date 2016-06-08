package hmda.validation.rules.lar.quality

import com.typesafe.config.ConfigFactory
import hmda.model.fi.lar.LoanApplicationRegister
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.{ BadValueUtils, LarEditCheckSpec }
import org.scalacheck.Gen

class Q040Spec extends LarEditCheckSpec with BadValueUtils {
  val config = ConfigFactory.load()
  val rateSpreadConfig = config.getDouble("hmda.validation.quality.Q040.rate-spread")

  property(s"All lars with rate spread equal to NA or less than $rateSpreadConfig% must pass") {
    forAll(larGen) { lar =>
      whenever(lar.rateSpread == "NA" || lar.rateSpread.toDouble <= rateSpreadConfig) {
        lar.mustPass
      }
    }
  }

  property("Lars with irrelevant purchaser types must pass") {
    forAll(larGen, intOutsideRange(1, 4)) { (lar, x) =>
      val newLar = lar.copy(purchaserType = x)
      newLar.mustPass
    }
  }

  property("Lars with irrelevant lien status must pass") {
    forAll(larGen, intOutsideRange(1, 2)) { (lar, x) =>
      val newLar = lar.copy(lienStatus = x)
      newLar.mustPass
    }
  }

  property(s"Lars with relevant purchaser type and lien status with rate spread > $rateSpreadConfig% must fail") {
    forAll(larGen, Gen.choose(1, 4), Gen.oneOf(1, 2), Gen.choose(rateSpreadConfig, Double.MaxValue)) {
      (lar, purchaser, lien, spread) =>
        val newLar = lar.copy(purchaserType = purchaser, lienStatus = lien, rateSpread = spread.toString)
        newLar.mustFail
    }
  }

  override def check: EditCheck[LoanApplicationRegister] = Q040
}
