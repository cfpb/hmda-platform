package hmda.calculator.api.model

import java.time.LocalDate

object RateSpreadRequest {

  case class RateSpreadBody(actionTakenType: Int,
                            loanTerm: Int,
                            amortizationType: String,
                            apr: Double,
                            lockInDate: LocalDate,
                            reverseMortgage: Int) {

    def toCSV: String =
      s"$actionTakenType,$loanTerm,$amortizationType,$apr,$lockInDate,$reverseMortgage"
  }
}
