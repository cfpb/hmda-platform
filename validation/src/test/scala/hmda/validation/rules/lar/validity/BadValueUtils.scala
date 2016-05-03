package hmda.validation.rules.lar.validity

import hmda.model.fi.lar.LoanApplicationRegister
import hmda.parser.fi.lar.LarGenerators
import org.scalacheck.Gen

trait BadValueUtils extends LarGenerators {

  def intOutsideRange(lower: Int, upper: Int): Gen[Int] = {
    val belowRange = Gen.choose(Integer.MIN_VALUE, lower - 1)
    val aboveRange = Gen.choose(upper + 1, Integer.MAX_VALUE)
    Gen.oneOf(belowRange, aboveRange)
  }

  val badPurchaserTypeGen: Gen[Int] = intOutsideRange(1, 9)

  val badPurchaserTypeLarGen: Gen[LoanApplicationRegister] = {
    for {
      lar <- larGen
      pt <- badPurchaserTypeGen
    } yield lar.copy(purchaserType = pt)
  }
}
