package hmda.validation.rules.lar.validity

import hmda.model.fi.lar.LoanApplicationRegister
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.LarEditCheckSpec
import org.scalacheck.Gen

class V210Spec extends LarEditCheckSpec {
  property("All date formats must be either NA or in the format 'yyyyMMdd'") {
    forAll(larGen) { lar =>
      lar.mustPass
    }
  }

  val invalidDate: Gen[String] = Gen.alphaStr.filter(_ != "NA")

  property("A lar with an invalid date must fail") {
    forAll(larGen, invalidDate) { (lar: LoanApplicationRegister, date: String) =>
      val badLoan = lar.loan.copy(applicationDate = date)
      val badLar = lar.copy(loan = badLoan)
      badLar.mustFail
    }
  }

  val invalidYearGen: Gen[Int] = Gen.choose(1000, 1999)

  property("A lar dated with an incorrect century must fail") {
    forAll(larGen, invalidYearGen) { (lar, year) =>
      val badLoan = lar.loan.copy(applicationDate = year + "0101")
      val badLar = lar.copy(loan = badLoan)
      badLar.mustFail
    }
  }

  property("A lar missing part of the date must fail") {
    forAll(larGen) { lar =>
      val badLoan = lar.loan.copy(applicationDate = "200001")
      val badLar = lar.copy(loan = badLoan)
      badLar.mustFail
    }
  }

  val invalidMonthGen: Gen[Int] = Gen.choose(13, 99)

  property("A lar with an invalid month must fail") {
    forAll(larGen, invalidMonthGen) { (lar, month) =>
      val badLoan = lar.loan.copy(applicationDate = "2000" + month + "01")
      val badLar = lar.copy(loan = badLoan)
      badLar.mustFail
    }
  }

  val invalidDayGen: Gen[Int] = Gen.choose(32, 99)

  property("A lar with an invalid day must fail") {
    forAll(larGen, invalidDayGen) { (lar, day) =>
      val badLoan = lar.loan.copy(applicationDate = "200012" + day)
      val badLar = lar.copy(loan = badLoan)
      badLar.mustFail
    }
  }

  override def check: EditCheck[LoanApplicationRegister] = V210
}
