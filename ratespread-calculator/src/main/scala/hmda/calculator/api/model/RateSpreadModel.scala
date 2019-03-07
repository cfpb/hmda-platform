package hmda.calculator.api.model

object RateSpreadModel {

  case class RateSpread(actionTakenType: String,
                        loanTerm: String,
                        amortizationType: String,
                        apr: String,
                        lockInDate: String,
                        reverseMortgage: String) {
    def toCSV: String =
      s"$actionTakenType,$loanTerm,$amortizationType,$apr,$lockInDate,$reverseMortgage"
  }
  case class LoanCheckDigitResponse(loanIds: Seq[RateSpread])
  case class RateSpreadCheck(rateSpread: String)
  case class RateSpreadValidated(uli: String, isValid: Boolean) {
    def toCSV: String = s"$uli,$isValid"
  }

}
