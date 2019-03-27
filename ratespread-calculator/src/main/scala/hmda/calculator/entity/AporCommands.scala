package hmda.calculator.entity

import java.time.{LocalDate, ZoneId}
import java.time.temporal.IsoFields

import hmda.calculator.api.CalculatedRateSpreadModel
import hmda.calculator.api.CalculatedRateSpreadModel.CalculatedRateSpread

import scala.collection.mutable.Map
import scala.util.{Failure, Success, Try}

object APORCommands {

  def getRateSpreadResponse(actionTakenType: Int,
                            loanTerm: Int,
                            amortizationType: RateType,
                            apr: Double,
                            lockInDate: LocalDate,
                            reverseMortgage: Int): CalculatedRateSpread = {

    if (!validLoanTerm(loanTerm)) {
      println("bad loan term")
      CalculatedRateSpreadModel.CalculatedRateSpread("")

    } else if (rateSpreadNA(actionTakenType, reverseMortgage)) {
      println("bad loan term")
      CalculatedRateSpreadModel.CalculatedRateSpread("")

    } else {
      aporForDateAndLoanTerm(loanTerm, amortizationType, lockInDate) match {
        case Some(apor) =>
          CalculatedRateSpreadModel.CalculatedRateSpread(
            calculateRateSpread(apr, apor).toString)
      }
    }
  }

  def findRateType(rateType: String): RateType = rateType match {
    case "FixedRate"    => FixedRate
    case "VariableRate" => VariableRate
  }

  private def calculateRateSpread(apr: Double, apor: Double): BigDecimal = {
    BigDecimal(apr - apor).setScale(3, BigDecimal.RoundingMode.HALF_UP)
  }

  private def aporForDateAndLoanTerm(loanTerm: Int,
                                     amortizationType: RateType,
                                     lockInDate: LocalDate): Option[Double] = {

    var aporList = Map[String, APOR]()

    if (amortizationType == FixedRate) {
      aporList = AporEntity.fixedRateAPORMap
    } else if (amortizationType == VariableRate) {
      aporList = AporEntity.variableRateAPORMap

    }

    val aporData = aporList.values.find { apor =>
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

  private def validLoanTerm(loanTerm: Int): Boolean =
    loanTerm >= 1 && loanTerm <= 50

  private def rateSpreadNA(actionTakenType: Int,
                           reverseMortgage: Int): Boolean = {
    (actionTakenType != 1 && actionTakenType != 2 && actionTakenType != 8) ||
    (reverseMortgage != 2)
  }

  private def weekNumberForDate(date: LocalDate): Int = {
    val zoneId = ZoneId.systemDefault()
    val weekField = IsoFields.WEEK_OF_WEEK_BASED_YEAR
    val dateTime = date.atStartOfDay(zoneId)
    dateTime.get(weekField)
  }

  private def weekYearForDate(date: LocalDate): Int = {
    val zoneId = ZoneId.systemDefault()
    val weekField = IsoFields.WEEK_BASED_YEAR
    val dateTime = date.atStartOfDay(zoneId)
    dateTime.get(weekField)
  }

}
