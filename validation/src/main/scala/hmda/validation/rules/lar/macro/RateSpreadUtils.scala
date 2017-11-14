package hmda.validation.rules.lar.`macro`

import scala.util.{Failure, Success, Try}

trait RateSpreadUtils {

  def rateSpreatToInt(rateSpreadStr: String): Int = {
    val str = rateSpreadStr.substring(1, rateSpreadStr.length)
    val t = Try(str.toInt)
    t match {
      case Success(rateSpread) => rateSpread
      case Failure(_) => 0
    }
  }

  def rateSpreadToDouble(rateSpreadStr: String): Double = {
    val str = rateSpreadStr.substring(1, rateSpreadStr.length)
    val t = Try(str.toDouble)
    t match {
      case Success(rateSpread) => rateSpread
      case Failure(_) => 0.0
    }
  }

}
