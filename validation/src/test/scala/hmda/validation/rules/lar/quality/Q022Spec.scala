package hmda.validation.rules.lar.quality

import hmda.parser.fi.lar.LarGenerators
import hmda.validation.dsl.Success
import hmda.validation.dsl.Failure
import hmda.validation.rules.lar.BadValueUtils
import org.scalacheck.Gen
import org.scalatest.{ MustMatchers, PropSpec }
import org.scalatest.prop.PropertyChecks

import scala.concurrent.{ ExecutionContext, Future }

class Q022Spec extends PropSpec with PropertyChecks with MustMatchers with LarGenerators with BadValueUtils {
  implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global

  property("Passes if activity year is within two years after application date") {
    forAll(larGen, Gen.choose(0, 2), dateGen) { (lar, x, date) =>
      val newLoan = lar.loan.copy(applicationDate = date.toString)
      val newLar = lar.copy(loan = newLoan)
      val applicationYear = newLar.loan.applicationDate.substring(0, 4).toInt
      Q022(newLar, applicationYear + x) mustBe Success()
      Q022(newLar, Future(applicationYear + x)).map(x => x mustBe Success())
    }
  }

  property("Fails if activity year is not within two years after application date") {
    forAll(larGen, intOutsideRange(0, 2), dateGen) { (lar, x, date) =>
      val newLoan = lar.loan.copy(applicationDate = date.toString)
      val newLar = lar.copy(loan = newLoan)
      val applicationYear = newLar.loan.applicationDate.substring(0, 4).toInt
      Q022(newLar, applicationYear + x) mustBe a[Failure]
      Q022(newLar, Future(applicationYear + x)).map(x => x mustBe a[Failure])
    }
  }

  property("Passes if activity year is equal to NA") {
    forAll(larGen) { lar =>
      val newLoan = lar.loan.copy(applicationDate = "NA")
      val newLar = lar.copy(loan = newLoan)

      Q022(newLar, 2099) mustBe a[Success]
      Q022(newLar, Future(2099)).map(x => x mustBe Success())
    }
  }
}
