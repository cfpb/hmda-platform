package hmda.validation.rules.lar.quality

import hmda.model.fi.lar.LarGenerators
import hmda.validation.context.ValidationContext
import hmda.validation.dsl.Success
import hmda.validation.dsl.Failure
import hmda.validation.rules.lar.BadValueUtils
import org.scalacheck.Gen
import org.scalatest.{ MustMatchers, PropSpec }
import org.scalatest.prop.PropertyChecks

class Q022Spec extends PropSpec with PropertyChecks with MustMatchers with LarGenerators with BadValueUtils {

  property("Passes if activity year is within two years after application date") {
    forAll(larGen, Gen.choose(0, 2), dateGen) { (lar, x, date) =>
      val newLoan = lar.loan.copy(applicationDate = date.toString)
      val newLar = lar.copy(loan = newLoan)
      val applicationYear = newLar.loan.applicationDate.substring(0, 4).toInt

      val ctx = ValidationContext(None, Some(applicationYear + x))

      Q022.inContext(ctx)(newLar) mustBe Success()
    }
  }

  property("Fails if activity year is not within two years after application date") {
    forAll(larGen, intOutsideRange(0, 2), dateGen) { (lar, x, date) =>
      val newLoan = lar.loan.copy(applicationDate = date.toString)
      val newLar = lar.copy(loan = newLoan)
      val applicationYear = newLar.loan.applicationDate.substring(0, 4).toInt

      val ctx = ValidationContext(None, Some(applicationYear + x))

      Q022.inContext(ctx)(newLar) mustBe Failure()
    }
  }

  property("Passes if activity year is equal to NA") {
    forAll(larGen) { lar =>
      val newLoan = lar.loan.copy(applicationDate = "NA")
      val newLar = lar.copy(loan = newLoan)

      val ctx = ValidationContext(None, Some(2099))
      Q022.inContext(ctx)(newLar) mustBe a[Success]
    }
  }

  property("Passes if activity year is not provided in ValidationContext") {
    forAll(larGen) { lar =>
      val ctx = ValidationContext(None, None)
      Q022.inContext(ctx)(lar) mustBe a[Success]
    }
  }
}
