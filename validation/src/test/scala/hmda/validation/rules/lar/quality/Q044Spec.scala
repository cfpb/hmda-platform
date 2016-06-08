package hmda.validation.rules.lar.quality

import com.typesafe.config.ConfigFactory
import hmda.model.fi.lar.LoanApplicationRegister
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.{ BadValueUtils, LarEditCheckSpec }
import org.scalacheck.Gen

class Q044Spec extends LarEditCheckSpec with BadValueUtils {

  val config = ConfigFactory.load()
  val rateSpread = config.getDouble("hmda.validation.quality.Q044.rateSpread")

  property("Valid if action not 1") {
    forAll(larGen, intOtherThan(1)) { (lar, x) =>
      val newLar = lar.copy(actionTakenType = x)
      newLar.mustPass
    }
  }

  property("Valid if lien not 1") {
    forAll(larGen, intOtherThan(1)) { (lar, x) =>
      val newLar = lar.copy(lienStatus = x)
      newLar.mustPass
    }
  }

  property("Valid when ratespread not numeric") {
    forAll(larGen, Gen.alphaStr) { (lar, x) =>
      val newLar = lar.copy(rateSpread = x)
      newLar.mustPass
    }
  }

  property(s"Valid when ratespread less than or equal to $rateSpread") {
    forAll(larGen, Gen.choose(Double.MinValue, rateSpread)) { (lar, x) =>
      val newLar = lar.copy(rateSpread = x.toString)
      newLar.mustPass
    }
  }

  property("Valid when HOEPA status = 1") {
    forAll(larGen) { (lar) =>
      val newLar = lar.copy(hoepaStatus = 1)
      newLar.mustPass
    }
  }

  property("Invalid when hoepa not 1 and other conditions met") {
    forAll(larGen, Gen.choose(rateSpread + .1, Double.MaxValue), intOtherThan(1)) { (lar, r, h) =>
      val newLar = lar.copy(
        rateSpread = r.toString,
        hoepaStatus = h,
        actionTakenType = 1,
        lienStatus = 1
      )
      newLar.mustFail
    }
  }

  override def check: EditCheck[LoanApplicationRegister] = Q044
}
