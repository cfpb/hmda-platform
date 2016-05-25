package hmda.validation.rules.lar.validity

import hmda.model.fi.lar.LoanApplicationRegister
import hmda.validation.dsl.{ Failure, Success }
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.LarEditCheckSpec
import org.scalacheck.Gen

class V355Spec extends LarEditCheckSpec with BadValueUtils {

  property("If agency code is relevant, lar with valid denial reasons must pass") {
    forAll(larGen) { lar =>
      whenever(agencyCodeRelevant(lar)) {
        lar.mustPass
      }
    }
  }

  property("If agency code is 1 and action taken is not 3 or 7, lar with valid denial reasons must pass") {
    forAll(larGen) { lar =>
      val newLar = lar.copy(agencyCode = 1)
      whenever(agencyCodeAndActionTakenRelevant(newLar)) {
        newLar.mustPass
      }
    }
  }

  val irrelevantAgencyCode: Gen[Int] = Gen.oneOf(4, 6, 8)
  property("If both agency code and action taken are irrelevant, lar should pass") {
    forAll(larGen, irrelevantAgencyCode) { (lar: LoanApplicationRegister, x: Int) =>
      val newLar = lar.copy(agencyCode = x)
      whenever(!agencyCodeAndActionTakenRelevant(newLar)) {
        newLar.mustPass
      }
    }
  }

  val invalidDenialCode: Gen[Int] = intOutsideRange(1, 9)
  property("If both agency code and action taken are relevant and denial is invalid, lar should fail") {
    forAll(larGen, invalidDenialCode) { (lar: LoanApplicationRegister, x: Int) =>
      val invalidDenial = lar.denial.copy(reason1 = x.toString)
      val invalidLar = lar.copy(denial = invalidDenial)
      whenever(agencyCodeRelevant(invalidLar) || agencyCodeAndActionTakenRelevant(invalidLar)) {
        invalidLar.mustFail
      }
    }
  }

  def agencyCodeRelevant(lar: LoanApplicationRegister) = {
    List(2, 3, 5, 7, 9).contains(lar.agencyCode)
  }

  def agencyCodeAndActionTakenRelevant(lar: LoanApplicationRegister) = {
    lar.agencyCode == 1 && !List(3, 7).contains(lar.actionTakenType)
  }

  override def check: EditCheck[LoanApplicationRegister] = V355
}
