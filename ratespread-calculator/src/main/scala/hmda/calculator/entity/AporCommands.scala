package hmda.calculator.entity

import java.time.LocalDate
import java.time.format.DateTimeFormatter

import hmda.messages.CommonMessages.Command

import scala.util.Try

object APORCommands {

  object CalculateRateSpread {
    def fromCsv(s: String): Option[CalculateRateSpread] = {
      val values = s.split(',').map(_.trim)
      val actionTakenType = Try(values.head.toInt)
      val loanTerm = Try(values(1).toInt)
      val amortizationType = Try(findRateType(values(2)))
      val apr = Try(values(3).toDouble)
      val lockInDate = Try(
        LocalDate.parse(values(4), DateTimeFormatter.ISO_LOCAL_DATE))
      val reverseMortgage = Try(values(5).toInt)

      val fields: List[Try[Any]] = List(actionTakenType,
                                        loanTerm,
                                        amortizationType,
                                        apr,
                                        lockInDate,
                                        reverseMortgage)

      if (fields.forall(_.isSuccess)) {
        Some(
          CalculateRateSpread(
            actionTakenType.get,
            loanTerm.get,
            amortizationType.get,
            apr.get,
            lockInDate.get,
            reverseMortgage.get
          ))
      } else None
    }

    def findRateType(rateType: String): RateType = rateType match {
      case "FixedRate"    => FixedRate
      case "VariableRate" => VariableRate
    }
  }
  case class CalculateRateSpread(
      actionTakenType: Int,
      loanTerm: Int,
      amortizationType: RateType,
      apr: Double,
      lockInDate: LocalDate,
      reverseMortgage: Int
  ) {
    def toCSV: String =
      s"$actionTakenType,$loanTerm,${amortizationType.toString},$apr,${lockInDate.toString},$reverseMortgage"
  }
}
