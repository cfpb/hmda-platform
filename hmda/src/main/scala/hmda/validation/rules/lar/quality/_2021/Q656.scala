package hmda.validation.rules.lar.quality._2021

import hmda.model.filing.lar.LoanApplicationRegister
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax.PredicateOps
import hmda.validation.dsl.ValidationResult
import hmda.validation.rules.EditCheck

import scala.util.Try


object Q656 extends EditCheck[LoanApplicationRegister] {
  override def name: String = "Q656"

  override def apply(lar: LoanApplicationRegister): ValidationResult ={
    val invalidExemptCode= "1111"

    val interestRate = Try(lar.loan.interestRate.toDouble).getOrElse(-1.0)
    val totalLoanCosts = Try(lar.loanDisclosure.totalLoanCosts.toDouble).getOrElse(-1.0)
    val totalPointsAndFees = Try(lar.loanDisclosure.totalPointsAndFees.toDouble).getOrElse(-1.0)
    val originationCharges = Try(lar.loanDisclosure.originationCharges.toDouble).getOrElse(-1.0)
    val discountPoints = Try(lar.loanDisclosure.discountPoints.toDouble).getOrElse(-1.0)
    val lenderCredits = Try(lar.loanDisclosure.lenderCredits.toDouble).getOrElse(-1.0)
    val rateSpread  = Try(lar.loan.rateSpread.toDouble).getOrElse(-1.0)

      (lar.loan.ULI not equalTo(invalidExemptCode)) and
      (interestRate not equalTo(invalidExemptCode.toDouble)) and
      (lar.loan.prepaymentPenaltyTerm not equalTo(invalidExemptCode)) and
      (lar.loan.debtToIncomeRatio not equalTo(invalidExemptCode)) and
      (lar.loan.combinedLoanToValueRatio not equalTo(invalidExemptCode)) and
      (lar.loan.loanTerm not equalTo(invalidExemptCode)) and
      (lar.loan.introductoryRatePeriod not equalTo(invalidExemptCode)) and
      (lar.geography.street not equalTo(invalidExemptCode)) and
      (lar.geography.city not equalTo(invalidExemptCode)) and
      (lar.geography.state not equalTo(invalidExemptCode)) and
      (lar.geography.zipCode not equalTo(invalidExemptCode)) and
      (rateSpread not equalTo(invalidExemptCode.toDouble)) and
      (totalLoanCosts not equalTo(invalidExemptCode.toDouble)) and
      (totalPointsAndFees not equalTo(invalidExemptCode.toDouble)) and
      (originationCharges not equalTo(invalidExemptCode.toDouble)) and
      (discountPoints not equalTo(invalidExemptCode.toDouble)) and
      (lenderCredits not equalTo(invalidExemptCode.toDouble)) and
      (lar.property.propertyValue not equalTo(invalidExemptCode)) and
      (lar.property.multiFamilyAffordableUnits not equalTo(invalidExemptCode)) and
      (lar.larIdentifier.NMLSRIdentifier not equalTo(invalidExemptCode))

  }
}