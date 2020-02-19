package hmda.calculator.parser

import java.time.LocalDate
import java.time.format.DateTimeFormatter

import hmda.calculator.api.model.RateSpreadRequest
import hmda.calculator.apor.APORCommands

import scala.util.Try

object RateSpreadCSVParser {

  def fromCsv(s: String): RateSpreadRequest = {
    val values = s.split(',').map(_.trim)
    val actionTakenType = Try(values.head.toInt)
    val loanTerm = Try(values(1).toInt)
    val amortizationType = Try(APORCommands.findRateType(values(2)))
    val apr = Try(values(3).toDouble)
    val lockInDate = Try(
      LocalDate.parse(values(4), DateTimeFormatter.ISO_LOCAL_DATE))
    val reverseMortgage = Try(values(5).toInt)
    RateSpreadRequest(actionTakenType.get,
                   loanTerm.get,
                   amortizationType.get.toString,
                   apr.get,
                   lockInDate.get,
                   reverseMortgage.get)

  }
}
