package hmda.validation.rules.lar.quality._2022

import hmda.model.filing.lar.LarGenerators._
import hmda.model.filing.lar.LoanApplicationRegister
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.LarEditCheckSpec

class Q659_1Spec extends LarEditCheckSpec {
  override def check: EditCheck[LoanApplicationRegister] = Q659_1

  val invalidExemptCodes = List("888", "88888")
  property("Exempt field should not have a value of 888, or 88888") {
    forAll(larGen) { lar =>
      invalidExemptCodes.foreach(invalidExemptCode => {
        lar.copy(applicant = lar.applicant.copy(age = invalidExemptCode.toInt)).mustFail
        lar.copy(applicant = lar.applicant.copy(age = invalidExemptCode.toInt / 2)).mustPass
        lar.copy(coApplicant = lar.coApplicant.copy(creditScore = invalidExemptCode.toInt)).mustFail
        lar.copy(coApplicant = lar.coApplicant.copy(creditScore = invalidExemptCode.toInt / 2)).mustPass
        lar.copy(geography = lar.geography.copy(street = invalidExemptCode)).mustFail
        lar.copy(geography = lar.geography.copy(street = s"$invalidExemptCode ave.")).mustPass
        lar.copy(income = invalidExemptCode).mustFail
        lar.copy(income = s"$$$invalidExemptCode").mustPass
        lar.copy(loan = lar.loan.copy(rateSpread = invalidExemptCode)).mustFail
        lar.copy(loan = lar.loan.copy(rateSpread = s"$invalidExemptCode.0")).mustFail
        lar.copy(loan = lar.loan.copy(rateSpread = s"123$invalidExemptCode.0")).mustPass
        lar.copy(loanDisclosure = lar.loanDisclosure.copy(totalLoanCosts = invalidExemptCode)).mustFail
        lar.copy(loanDisclosure = lar.loanDisclosure.copy(totalLoanCosts = s"$invalidExemptCode.0")).mustFail
        lar.copy(loanDisclosure = lar.loanDisclosure.copy(totalLoanCosts = s"123$invalidExemptCode.0")).mustPass
        lar.copy(property = lar.property.copy(multiFamilyAffordableUnits = invalidExemptCode)).mustFail
        lar.copy(larIdentifier = lar.larIdentifier.copy(NMLSRIdentifier = invalidExemptCode)).mustFail
      })
    }
  }
}