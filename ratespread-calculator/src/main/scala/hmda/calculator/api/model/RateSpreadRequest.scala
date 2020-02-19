package hmda.calculator.api.model

import java.time.LocalDate

case class RateSpreadRequest(actionTakenType: Int,
                            loanTerm: Int,
                            amortizationType: String,
                            apr: Double,
                            lockInDate: LocalDate,
                            reverseMortgage: Int) {

  def toCSV: String =
    s"$actionTakenType,$loanTerm,$amortizationType,$apr,$lockInDate,$reverseMortgage"
}
