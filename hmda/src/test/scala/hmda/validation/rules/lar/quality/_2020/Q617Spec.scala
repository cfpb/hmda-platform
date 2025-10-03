package hmda.validation.rules.lar.quality._2020

import hmda.model.filing.lar.LarGenerators._
import hmda.model.filing.lar.LoanApplicationRegister
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.LarEditCheckSpec
import hmda.model.filing.lar.enums.Conventional

class Q617Spec extends LarEditCheckSpec {
  override def check: EditCheck[LoanApplicationRegister] = Q617

  property(
    "Combined loan-to-value ratio should be greater than calculated loan-to-value ratio") {
    forAll(larGen) { lar =>
      whenever(
        lar.loan.loanType != Conventional || lar.loan.combinedLoanToValueRatio == "NA" || lar.loan.combinedLoanToValueRatio == "Exempt") {
        lar.mustPass
      }

      val appLar =
        lar.copy(loan =
          lar.loan.copy(loanType = Conventional, amount = 150000, combinedLoanToValueRatio = "25.860"))
      appLar
        .copy(property = appLar.property.copy(propertyValue = "500000.00"))
        .mustFail
      appLar
        .copy(property = appLar.property.copy(propertyValue = "570000.00"))
        .mustPass
      appLar
        .copy(property = appLar.property.copy(propertyValue = "580000.00"))
        .mustPass
    }
  }

  property("Calculation should match number of digits reported in CLTV ratio") {
    forAll(larGen) { lar =>
      val appLar =
        lar.copy(
          loan = lar.loan.copy(loanType = Conventional, amount = 50.4, combinedLoanToValueRatio = "50"))
      appLar
        .copy(property = appLar.property.copy(propertyValue = "100.0"))
        .mustPass

      val roundLar =
        lar.copy(
          loan = lar.loan.copy(loanType = Conventional, amount = 50.5, combinedLoanToValueRatio = "50"))
      roundLar
        .copy(property = roundLar.property.copy(propertyValue = "100.0"))
        .mustFail

      val decLarAmount =
        lar.copy(loan = lar.loan.copy(loanType = Conventional, amount = 30))
      val decLar = decLarAmount
        .copy(property = roundLar.property.copy(propertyValue = "31"))

      decLar
        .copy(loan = decLar.loan.copy(combinedLoanToValueRatio = "96.77"))
        .mustPass
      decLar
        .copy(loan = decLar.loan.copy(combinedLoanToValueRatio = "96.7"))
        .mustFail
    }
  }

  property(
    "Calculation should not differentiate between a whole number and a whole number with .0") {
    forAll(larGen) { lar =>
      val failLar =
        lar.copy(
          loan =
            lar.loan.copy(loanType = Conventional, amount = 55402.5, combinedLoanToValueRatio = "55.0"))
      failLar
        .copy(property = failLar.property.copy(propertyValue = "100000.0"))
        .mustPass
      val passLar =
        lar.copy(
          loan =
            lar.loan.copy(loanType = Conventional, amount = 55402.5, combinedLoanToValueRatio = "55"))
      passLar
        .copy(property = passLar.property.copy(propertyValue = "100000.0"))
        .mustPass
    }
  }
}
