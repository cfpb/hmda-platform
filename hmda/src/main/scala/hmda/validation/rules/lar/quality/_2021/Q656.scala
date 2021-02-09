package hmda.validation.rules.lar.quality._2021

import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.filing.lar.enums._
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax.PredicateOps
import hmda.validation.dsl.ValidationResult
import hmda.validation.rules.EditCheck


object Q656 extends EditCheck[LoanApplicationRegister] {
  override def name: String = "Q656"

  override def apply(lar: LoanApplicationRegister): ValidationResult ={
    val invalidExemptCode= "1111"

    (lar.loan.ULI not equalTo(invalidExemptCode)) and
      (lar.loan.rateSpread not equalTo(invalidExemptCode)) and
      (lar.loan.interestRate not equalTo(invalidExemptCode)) and
      (lar.loan.prepaymentPenaltyTerm not equalTo(invalidExemptCode)) and
      (lar.loan.debtToIncomeRatio not equalTo(invalidExemptCode)) and
      (lar.loan.combinedLoanToValueRatio not equalTo(invalidExemptCode)) and
      (lar.loan.loanTerm not equalTo(invalidExemptCode)) and
      (lar.loan.introductoryRatePeriod not equalTo(invalidExemptCode)) and
      (lar.geography.street not equalTo(invalidExemptCode)) and
      (lar.geography.city not equalTo(invalidExemptCode)) and
      (lar.geography.state not equalTo(invalidExemptCode)) and
      (lar.geography.zipCode not equalTo(invalidExemptCode)) and
      (lar.loan.rateSpread not equalTo(invalidExemptCode)) and
      (lar.loanDisclosure.totalLoanCosts not equalTo(invalidExemptCode)) and
      (lar.loanDisclosure.totalPointsAndFees not equalTo(invalidExemptCode)) and
      (lar.loanDisclosure.originationCharges not equalTo(invalidExemptCode)) and
      (lar.loanDisclosure.discountPoints not equalTo(invalidExemptCode)) and
      (lar.loanDisclosure.lenderCredits not equalTo(invalidExemptCode)) and
      (lar.property.propertyValue not equalTo(invalidExemptCode)) and
      (lar.property.multiFamilyAffordableUnits not equalTo(invalidExemptCode)) and
      (lar.larIdentifier.NMLSRIdentifier not equalTo(invalidExemptCode))
  }
}