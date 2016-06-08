package hmda.validation.rules.lar.quality

import com.typesafe.config.ConfigFactory
import hmda.model.fi.lar.LoanApplicationRegister
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.{ BadValueUtils, LarEditCheckSpec }
import org.scalacheck.Gen

class Q066Spec extends LarEditCheckSpec with BadValueUtils {
  val config = ConfigFactory.load()
  val rateSpreadConfig = config.getDouble("hmda.validation.quality.Q066.rate-spread")

  property("All lars with rate spread equal to NA must pass") {
    forAll(larGen) { lar =>
      whenever(lar.rateSpread == "NA") {
        lar.mustPass
      }
    }
  }

  property(s"Lars with rate spread less than $rateSpreadConfig must pass") {
    forAll(larGen, Gen.choose(0.0, rateSpreadConfig - 0.1)) { (lar, x) =>
      val newLar = lar.copy(rateSpread = x.toString)
      newLar.mustPass
    }
  }

  property(s"Lars with rate spread greater than or equal to $rateSpreadConfig must fail") {
    forAll(larGen, Gen.choose(rateSpreadConfig, Double.MaxValue)) { (lar, x) =>
      val newLar = lar.copy(rateSpread = x.toString)
      newLar.mustFail
    }
  }

  property("Lars with a random string as rate spread must fail") {
    forAll(larGen, Gen.alphaStr.filter(_ != "NA")) { (lar, x) =>
      val newLar = lar.copy(rateSpread = x)
      newLar.mustFail
    }
  }

  override def check: EditCheck[LoanApplicationRegister] = Q066
}
