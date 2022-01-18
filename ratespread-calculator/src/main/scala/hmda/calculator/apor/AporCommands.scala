package hmda.calculator.apor

import java.time.format.DateTimeFormatter
import java.time.temporal.IsoFields
import java.time.{ LocalDate, ZoneId }

import hmda.calculator.api.RateSpreadResponse
import hmda.calculator.api.model.RateSpreadRequest

import scala.collection.mutable
import scala.util.{ Failure, Success, Try }

object APORCommands {

  def getRateSpreadResponse(rateSpread: RateSpreadRequest): RateSpreadResponse = {

    //1.) Validate the Rate Spread Data Otherwise fail

    //2.) Calculate RateSpreadRequest and return CalculatedRateSpread if validation passes
    val values           = rateSpread.toCSV.split(',').map(_.trim)
    val actionTakenType  = Try(values.head.toInt)
    val loanTerm         = Try(values(1).toInt)
    val amortizationType = Try(APORCommands.findRateType(values(2)))
    val apr              = Try(values(3).toDouble)
    val lockInDate       = Try(LocalDate.parse(values(4), DateTimeFormatter.ISO_LOCAL_DATE))
    val reverseMortgage  = Try(values(5).toInt)

    val rateSpreadResponse = APORCommands.getRateSpreadCalculation(
      actionTakenType.get,
      loanTerm.get,
      amortizationType.get,
      apr.get,
      lockInDate.get,
      reverseMortgage.get
    )

    rateSpreadResponse
  }

  def getRateSpreadCalculation(
                                actionTakenType: Int,
                                loanTerm: Int,
                                amortizationType: RateType,
                                apr: Double,
                                lockInDate: LocalDate,
                                reverseMortgage: Int
                              ): RateSpreadResponse =
    if (!validLoanTerm(loanTerm)) {
      RateSpreadResponse("bad loan term")

    } else if (rateSpreadNA(actionTakenType, reverseMortgage)) {
      RateSpreadResponse("rate spread NA")

    } else {
      aporForDateAndLoanTerm(loanTerm, amortizationType, lockInDate) match {
        case Some(apor) =>
          RateSpreadResponse(calculateRateSpread(apr, apor).toString)
      }
    }

  def findRateType(rateType: String): RateType = rateType match {
    case "FixedRate"    => FixedRate
    case "VariableRate" => VariableRate
  }

  private def calculateRateSpread(apr: Double, apor: Double): BigDecimal =
    BigDecimal(apr - apor).setScale(3, BigDecimal.RoundingMode.HALF_UP)

  private def aporForDateAndLoanTerm(loanTerm: Int, amortizationType: RateType, lockInDate: LocalDate): Option[Double] = {

    val aporData = getAporMap(amortizationType).values.find { apor =>
      weekNumberForDate(apor.rateDate) == weekNumberForDate(lockInDate) &&
        weekYearForDate(apor.rateDate) == weekYearForDate(lockInDate)
    }

    aporData match {
      case Some(data) =>
        Try(data.values(loanTerm - 1)) match {
          case Success(rate) => Some(rate)
          case Failure(e)    => None
        }
      case None => None
    }
  }

  private def getAporMap(amortizationType: RateType): mutable.Map[String, APOR] =
    if (amortizationType == FixedRate) {
      AporListEntity.fixedRateAPORMap
    } else {
      AporListEntity.variableRateAPORMap
    }

  private def validLoanTerm(loanTerm: Int): Boolean =
    loanTerm >= 1 && loanTerm <= 50

  private def rateSpreadNA(actionTakenType: Int, reverseMortgage: Int): Boolean =
    (actionTakenType != 1 && actionTakenType != 2 && actionTakenType != 8) ||
      (reverseMortgage != 2)

  private def weekNumberForDate(date: LocalDate): Int = {
    val zoneId    = ZoneId.systemDefault()
    val weekField = IsoFields.WEEK_OF_WEEK_BASED_YEAR
    val dateTime  = date.atStartOfDay(zoneId)
    dateTime.get(weekField)
  }

  private def weekYearForDate(date: LocalDate): Int = {
    val zoneId    = ZoneId.systemDefault()
    val weekField = IsoFields.WEEK_BASED_YEAR
    val dateTime  = date.atStartOfDay(zoneId)
    dateTime.get(weekField)
  }
}