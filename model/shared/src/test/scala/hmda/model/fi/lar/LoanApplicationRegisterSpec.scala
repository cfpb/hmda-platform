package hmda.model.fi.lar

import org.scalatest.{ MustMatchers, PropSpec }
import org.scalatest.prop.PropertyChecks

class LoanApplicationRegisterSpec extends PropSpec with PropertyChecks with MustMatchers with LarGenerators {

  property("must return value from friendly field name: Action Taken") {
    forAll(larGen) { (lar: LoanApplicationRegister) =>
      lar.valueOf("Type of Action Taken") mustBe lar.actionTakenType
    }
  }

  property("must return value from friendly field name: Preapprovals") {
    forAll(larGen) { (lar: LoanApplicationRegister) =>
      lar.valueOf("Preapprovals") mustBe lar.preapprovals
    }
  }

  property("must return value from friendly field name: Loan Amount") {
    forAll(larGen) { (lar: LoanApplicationRegister) =>
      lar.valueOf("Loan Amount") mustBe lar.loan.amount
    }
  }

  property("must return value from friendly field name: Applicant Race") {
    forAll(larGen) { (lar: LoanApplicationRegister) =>
      lar.valueOf("Applicant Race: 1") mustBe lar.applicant.race1
    }
  }

  property("must return value from friendly field name: Co-Applicant Ethnicity") {
    forAll(larGen) { (lar: LoanApplicationRegister) =>
      lar.valueOf("Co-applicant Ethnicity") mustBe lar.applicant.coEthnicity
    }
  }
}
