package hmda.validation.rules.lar.quality._2021

import hmda.model.filing.lar.LarGenerators._
import hmda.model.filing.lar.LoanApplicationRegister
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.LarEditCheckSpec

class Q656Spec extends LarEditCheckSpec {
  override def check: EditCheck[LoanApplicationRegister] = Q656
  val invalidExemptCode="1111"
  val validExemptCode="Exempt"
  property("Exempt alphanumeric field should not have a value of 1111") {
    forAll(larGen) { lar =>

      val appLar = lar.copy(loan = lar.loan.copy(ULI = invalidExemptCode))
        appLar.mustFail

      val appLar1 = lar.copy(loan = lar.loan.copy(rateSpread = invalidExemptCode))
      appLar1.mustFail

      val appLar2 = lar.copy(loan = lar.loan.copy(prepaymentPenaltyTerm = invalidExemptCode))
      appLar2.mustFail

      val appLar3 = lar.copy(loan = lar.loan.copy(debtToIncomeRatio = invalidExemptCode))
      appLar3.mustFail

      val appLar4 = lar.copy(loan = lar.loan.copy(combinedLoanToValueRatio = invalidExemptCode))
      appLar4.mustFail

      val appLar5= lar.copy(loan = lar.loan.copy(loanTerm = invalidExemptCode))
      appLar5.mustFail

      val appLar6 = lar.copy(loan = lar.loan.copy(introductoryRatePeriod = invalidExemptCode))
      appLar6.mustFail

      val appLar7 = lar.copy(geography = lar.geography.copy(street = invalidExemptCode))
      appLar7.mustFail

      val appLar8 = lar.copy(geography = lar.geography.copy(city = invalidExemptCode))
      appLar8.mustFail

      val appLar9 = lar.copy(geography = lar.geography.copy(state = invalidExemptCode))
      appLar9.mustFail

      val appLar10 = lar.copy(geography = lar.geography.copy(zipCode = invalidExemptCode))
      appLar10.mustFail

      val appLar11 = lar.copy(loanDisclosure = lar.loanDisclosure.copy(totalLoanCosts = invalidExemptCode))
      appLar11.mustFail

      val appLar12 = lar.copy(loanDisclosure = lar.loanDisclosure.copy(totalPointsAndFees = invalidExemptCode))
      appLar12.mustFail

      val appLar13 = lar.copy(loanDisclosure = lar.loanDisclosure.copy(originationCharges = invalidExemptCode))
      appLar13.mustFail

      val appLar14 = lar.copy(loanDisclosure = lar.loanDisclosure.copy(discountPoints = invalidExemptCode))
      appLar14.mustFail

      val appLar15 = lar.copy(loanDisclosure = lar.loanDisclosure.copy(lenderCredits = invalidExemptCode))
      appLar15.mustFail

      val appLar16 = lar.copy(property = lar.property.copy(propertyValue = invalidExemptCode))
      appLar16.mustFail

      val appLar17 = lar.copy(property = lar.property.copy(multiFamilyAffordableUnits = invalidExemptCode))
      appLar17.mustFail

      val appLar18 = lar.copy(larIdentifier = lar.larIdentifier.copy(NMLSRIdentifier = invalidExemptCode))
      appLar18.mustFail

      val appLar19 = lar.copy(loan = lar.loan.copy(rateSpread = validExemptCode))
      appLar19.mustPass

      val appLar20 = lar.copy(loan = lar.loan.copy(prepaymentPenaltyTerm = validExemptCode))
      appLar20.mustPass

      val appLar21 = lar.copy(loan = lar.loan.copy(debtToIncomeRatio = validExemptCode))
      appLar21.mustPass

      val appLar22 = lar.copy(loan = lar.loan.copy(combinedLoanToValueRatio = validExemptCode))
      appLar22.mustPass

    }
  }
}
