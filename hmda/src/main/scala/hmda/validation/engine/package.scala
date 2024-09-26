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
      case (2021, None)    => TsEngine2021
      case (2021, Some(_)) => TsEngine2021Q
      case (2022, Some(_)) => TsEngine2022Q
      case (2022, None)    => TsEngine2022
      case (2023, Some(_)) => TsEngine2023Q
      case (2023, None)    => TsEngine2023
      case (2024, Some(_)) => TsEngine2024Q
      case (2024, None)    => TsEngine2024
      case (2025, Some(_)) => TsEngine2025Q
      case (2025, None)    => TsEngine2025

      case _               => TsEngine2022 // TODO: determine what engine to pick if the user enters a year that is not covered
    }

  def selectTsLarEngine(year: Int, quarter: Option[String]): ValidationEngine[TransmittalLar] =
    (year, quarter) match {
      case (2018, None)    => TsLarEngine2018
      case (2019, None)    => TsLarEngine2019
      case (2020, None)    => TsLarEngine2020
      case (2020, Some(_)) => TsLarEngine2020Q
      case (2021, None)    => TsLarEngine2021
      case (2021, Some(_)) => TsLarEngine2021Q
      case (2022, Some(_)) => TsLarEngine2022Q
      case (2022, None)    => TsLarEngine2022
      case (2023, Some(_)) => TsLarEngine2023Q
      case (2023, None)    => TsLarEngine2023
      case (2024, Some(_)) => TsLarEngine2024Q
      case (2024, None)    => TsLarEngine2024
      case (2025, Some(_)) => TsLarEngine2025Q
      case (2025, None)    => TsLarEngine2025
      case _ =>
        TsLarEngine2022 // TODO: determine what engine to pick if the user enters a year that is not covered
    }

  def selectLarEngine(year: Int, quarter: Option[String]): ValidationEngine[LoanApplicationRegister] =
    (year, quarter) match {
      case (2018, None)     => LarEngine2018
      case (2019, None)     => LarEngine2019
      case (2020, None)     => LarEngine2020
      case (2020, Some(_))  => LarEngine2020Q
      case (2021, None)     => LarEngine2021
      case (2021, Some(_))  => LarEngine2021Q
      case (2022, Some(_))  => LarEngine2022Q
      case (2022, None)     => LarEngine2022
      case (2023, Some(_))  => LarEngine2023Q
      case (2023, None)     => LarEngine2023
      case (2024, Some(_))  => LarEngine2024Q
      case (2024, None)     => LarEngine2024
      case (2025, Some(_))  => LarEngine2025Q
      case (2025, None)     => LarEngine2025
      case _ =>
        LarEngine2022 // TODO: determine what engine to pick if the user enters a year that is not covered
    }
}