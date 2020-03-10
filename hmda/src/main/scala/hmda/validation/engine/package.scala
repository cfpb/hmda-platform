package hmda.validation

import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.filing.ts.{ TransmittalLar, TransmittalSheet }

package object engine {
  def selectTsEngine(year: Int, quarter: Option[String]): ValidationEngine[TransmittalSheet] =
    (year, quarter) match {
      case (2018, None)    => TsEngine2018
      case (2019, None)    => TsEngine2019
      case (2020, None)    => TsEngine2020
      case (2020, Some(_)) => TsEngine2020Q
      case _               => TsEngine2020 // TODO: determine what engine to pick if the user enters a year that is not covered
    }

  def selectTsLarEngine(year: Int, quarter: Option[String]): ValidationEngine[TransmittalLar] =
    (year, quarter) match {
      case (2018, None)    => TsLarEngine2018
      case (2019, None)    => TsLarEngine2019
      case (2020, None)    => TsLarEngine2020
      case (2020, Some(_)) => TsLarEngine2020Q
      case _ =>
        TsLarEngine2020 // TODO: determine what engine to pick if the user enters a year that is not covered
    }

  def selectLarEngine(year: Int, quarter: Option[String]): ValidationEngine[LoanApplicationRegister] =
    (year, quarter) match {
      case (2018, None)    => LarEngine2018
      case (2019, None)    => LarEngine2019
      case (2020, None)    => LarEngine2020
      case (2020, Some(_)) => LarEngine2020Q
      case _ =>
        LarEngine2020 // TODO: determine what engine to pick if the user enters a year that is not covered
    }
}