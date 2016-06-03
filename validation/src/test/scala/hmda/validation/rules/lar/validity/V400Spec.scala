package hmda.validation.rules.lar.validity

import hmda.model.fi.lar.{ Loan, LoanApplicationRegister }
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.{ BadValueUtils, LarEditCheckSpec }
import org.scalacheck.Gen

class V400Spec extends LarEditCheckSpec with BadValueUtils {
  property("Property Type must = 1,2, or 3") {
    forAll(larGen) { lar =>
      lar.mustPass
    }
  }

  val badPropertyTypeGen: Gen[Int] = intOutsideRange(1, 3)

  property("Property Type other than 1,2,3 is invalid") {
    forAll(larGen, badPropertyTypeGen) { (lar: LoanApplicationRegister, x: Int) =>
      val invalidLoan: Loan = lar.loan.copy(propertyType = x)
      val invalidLar: LoanApplicationRegister = lar.copy(loan = invalidLoan)
      invalidLar.mustFail
    }
  }

  override def check: EditCheck[LoanApplicationRegister] = V400
}
