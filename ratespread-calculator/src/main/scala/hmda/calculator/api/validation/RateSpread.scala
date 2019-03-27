package hmda.calculator.api.validation

import java.time.LocalDate
import java.time.format.DateTimeFormatter

import hmda.calculator.api.CalculatedRateSpreadModel.CalculatedRateSpread
import hmda.calculator.api.model.RateSpreadModel.RateSpread
import hmda.calculator.entity.{APORCommands, AporEntity, RateType}

import scala.util.Try

object RateSpread {

  def rateSpreadCalculation(rateSpread: RateSpread): CalculatedRateSpread = {

    //1.) Validate the Rate Spread Data Otherwise fail

    //2.) Calculate RateSpread and return CalculatedRateSpread if validation passes
    val values = rateSpread.toCSV.split(',').map(_.trim)
    val actionTakenType = Try(values.head.toInt)
    val loanTerm = Try(values(1).toInt)
    val amortizationType = Try(APORCommands.findRateType(values(2)))
    val apr = Try(values(3).toDouble)
    val lockInDate = Try(
      LocalDate.parse(values(4), DateTimeFormatter.ISO_LOCAL_DATE))
    val reverseMortgage = Try(values(5).toInt)

    val rateSpreadResponse = APORCommands.getRateSpreadResponse(
      actionTakenType.get,
      loanTerm.get,
      amortizationType.get,
      apr.get,
      lockInDate.get,
      reverseMortgage.get)

    val calculatedRateSpread = CalculatedRateSpread(
      rateSpreadResponse.rateSpread)
    calculatedRateSpread
  }

  def validateRateSpread(rs: String): Boolean = {
    true
  }
}
