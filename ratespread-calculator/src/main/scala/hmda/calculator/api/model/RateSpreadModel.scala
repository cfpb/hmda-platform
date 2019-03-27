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
  case class RateSpreadCheck(rateSpread: String)

}
