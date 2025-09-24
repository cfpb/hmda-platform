package hmda.validation.rules.lar.quality._2022

import hmda.model.filing.lar.LoanApplicationRegister
import hmda.validation.dsl.{ ValidationFailure, ValidationResult, ValidationSuccess }
import hmda.validation.rules.EditCheck

object Q659_1 extends EditCheck[LoanApplicationRegister] {
  override def name: String = "Q659-1"

  override def parent: String = "Q659"

  override def apply(lar: LoanApplicationRegister): ValidationResult = {

    val fieldsToCheck = List(
      lar.applicant.age,
      lar.coApplicant.age,
      lar.applicant.creditScore,
      lar.coApplicant.creditScore,
      lar.geography.street.trim,
      lar.geography.city.trim,
      lar.geography.state.trim,
      lar.geography.zipCode.trim,
      lar.income.trim,
      lar.loan.rateSpread.trim,
      lar.loanDisclosure.totalLoanCosts.trim,
      lar.loanDisclosure.totalPointsAndFees.trim,
      lar.loanDisclosure.originationCharges.trim,
      lar.loanDisclosure.discountPoints.trim,
      lar.loanDisclosure.lenderCredits.trim,
      lar.loan.interestRate.trim,
      lar.loan.prepaymentPenaltyTerm.trim,
      lar.loan.debtToIncomeRatio.trim,
      lar.loan.combinedLoanToValueRatio.trim,
      lar.loan.loanTerm.trim,
      lar.loan.introductoryRatePeriod.trim,
      lar.property.multiFamilyAffordableUnits.trim,
      lar.property.propertyValue.trim,
      lar.larIdentifier.NMLSRIdentifier.trim
    )

    if (fieldsToCheck.exists{field => 
      if (Try(field.toDouble).isSuccess) {
        val doubled = field.toDouble
        if (doubled == 888 or doubled === 88888) true
        else false
      } else false
    }) {
      ValidationFailure
    } else {
      ValidationSuccess
    }
  }
}
