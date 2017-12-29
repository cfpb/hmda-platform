package hmda.persistence.messages.commands.apor

import java.time.LocalDate
import java.time.format.DateTimeFormatter

import hmda.model.apor.{ APOR, FixedRate, RateType, VariableRate }
import hmda.persistence.messages.CommonMessages.Command

import scala.util.Try

object APORCommands {
  case class CreateApor(apor: APOR, rateType: RateType) extends Command
  /*
  object CalculateRateSpread {
    def apply(s: String): CalculateRateSpread = {
      val values = s.split(',').map(_.trim)
      val actionTakenType = values.head
      val amortizationType = values(1)
      val rateType = values(2)
      val apr = values(3)
      val lockInDate = values(4)
      val reverseMortgage = values(5)
      CalculateRateSpread(
        Try(actionTakenType.toInt).getOrElse(0),
        Try(amortizationType.toInt).getOrElse(0),
        findRateType(rateType),
        Try(apr.toDouble).getOrElse(0.0),
        LocalDate.parse(lockInDate, DateTimeFormatter.ISO_LOCAL_DATE),
        Try(reverseMortgage.toInt).getOrElse(0)
      )
    }

    def findRateType(rateType: String): RateType = rateType match {
      case "FixedRate" => FixedRate
      case "VariableRate" => VariableRate
    }

  }
    */
  case class CalculateRateSpread(
      actionTakenType: Int,
      loanTerm: Int,
      amortizationType: RateType,
      apr: Double,
      lockInDate: LocalDate,
      reverseMortgage: Int
  ) {
    def toCSV: String = s"$actionTakenType,$loanTerm,${amortizationType.toString},$apr,${lockInDate.toString},$reverseMortgage"
  }
}
