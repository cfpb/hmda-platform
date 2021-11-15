package hmda.validation.rules.lar.quality._2021

import hmda.model.filing.lar.LoanApplicationRegister
import hmda.validation.dsl.{ ValidationFailure, ValidationResult, ValidationSuccess }
import hmda.validation.rules.EditCheck

object Q659_1 extends EditCheck[LoanApplicationRegister] {
  override def name: String = "Q659-1"

  override def parent: String = "Q659"

  override def apply(lar: LoanApplicationRegister): ValidationResult = {
    val invalidExemptCodes = List("888", "88888")
    val fieldsToCheck = List(
      lar.applicant.age.toString,
      lar.coApplicant.age.toString,
      lar.applicant.creditScore.toString,
      lar.coApplicant.creditScore.toString,
      lar.geography.street,
      lar.geography.city,
      lar.geography.state,
      lar.geography.zipCode,
      lar.income,
      lar.loan.rateSpread,
      lar.loanDisclosure.totalLoanCosts,
      lar.loanDisclosure.totalPointsAndFees,
      lar.loanDisclosure.originationCharges,
      lar.loanDisclosure.discountPoints,
      lar.loanDisclosure.lenderCredits,
      lar.loan.interestRate,
      lar.loan.prepaymentPenaltyTerm,
      lar.loan.debtToIncomeRatio,
      lar.loan.combinedLoanToValueRatio,
      lar.loan.loanTerm,
      lar.loan.introductoryRatePeriod,
      lar.property.multiFamilyAffordableUnits,
      lar.larIdentifier.NMLSRIdentifier
    )

    if (fieldsToCheck.exists(invalidExemptCodes.contains)) {
      ValidationFailure
    } else {
      ValidationSuccess
    }
  }
}