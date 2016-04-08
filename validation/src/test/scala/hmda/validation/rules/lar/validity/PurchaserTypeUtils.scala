package hmda.validation.rules.lar.validity

import hmda.model.fi.lar.LoanApplicationRegister
import hmda.parser.fi.lar.LarGenerators
import org.scalacheck.Gen

trait PurchaserTypeUtils extends LarGenerators {
  val badPurchaserTypeGen: Gen[Int] = Gen.oneOf(Gen.negNum[Int], Gen.choose(10, Integer.MAX_VALUE))

  val badPurchaserTypeLarGen: Gen[LoanApplicationRegister] = {
    for {
      lar <- larGen
      pt <- badPurchaserTypeGen
    } yield lar.copy(purchaserType = pt)
  }
}
