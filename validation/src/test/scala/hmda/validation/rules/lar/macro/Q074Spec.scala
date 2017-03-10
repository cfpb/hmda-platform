package hmda.validation.rules.lar.`macro`

import hmda.model.fi.lar.LoanApplicationRegister
import hmda.validation.rules.AggregateEditCheck
import hmda.validation.rules.lar.`macro`.MacroEditTypes.LoanApplicationRegisterSource

class Q074Spec extends MacroSpec {

  val numOfLoanApplications = config.getInt("hmda.validation.macro.Q074.numOfLoanApplications")
  val multiplier = config.getDouble("hmda.validation.macro.Q074.numOfLarsMultiplier")

  def irrelevantLar(lar: LoanApplicationRegister) = {
    val relevantLoan = lar.loan.copy(propertyType = 2, loanType = 2, purpose = 3)
    lar.copy(purchaserType = 0, actionTakenType = 1, loan = relevantLoan)
  }
  def relevantLar(lar: LoanApplicationRegister) = {
    val relevantLoan = lar.loan.copy(propertyType = 2, loanType = 2, purpose = 3)
    lar.copy(purchaserType = 1, actionTakenType = 1, loan = relevantLoan)
  }

  val belowThreshold = numOfLoanApplications - 1
  val threshold = numOfLoanApplications

  property(s"be valid if fewer than $numOfLoanApplications specified loans") {
    val lars = larNGen(belowThreshold).sample.getOrElse(List[LoanApplicationRegister]())
    val validLarSource = newLarSource(lars, belowThreshold, relevantLar, irrelevantLar)
    validLarSource.mustPass
  }

  property(s"be valid if more than $numOfLoanApplications specified loans and count > $multiplier * sold") {
    val numOfRelevantLars = (threshold * multiplier).toInt + 1
    val lars = larNGen(threshold).sample.getOrElse(List[LoanApplicationRegister]())
    val validLarSource = newLarSource(lars, numOfRelevantLars, relevantLar, irrelevantLar)
    validLarSource.mustPass
  }

  property(s"be invalid if more than $numOfLoanApplications specified loans and count = $multiplier * sold") {
    val numOfRelevantLars = (threshold * multiplier).toInt
    val lars = larNGen(threshold).sample.getOrElse(List[LoanApplicationRegister]())
    val invalidLarSource = newLarSource(lars, numOfRelevantLars, relevantLar, irrelevantLar)
    invalidLarSource.mustFail
  }

  property(s"be invalid if more than $numOfLoanApplications specified loans and count < $multiplier * sold") {
    val numOfRelevantLars = (threshold * multiplier).toInt - 1
    val lars = larNGen(threshold).sample.getOrElse(List[LoanApplicationRegister]())
    val invalidLarSource = newLarSource(lars, numOfRelevantLars, relevantLar, irrelevantLar)
    invalidLarSource.mustFail
  }

  override def check: AggregateEditCheck[LoanApplicationRegisterSource, LoanApplicationRegister] = Q074
}
