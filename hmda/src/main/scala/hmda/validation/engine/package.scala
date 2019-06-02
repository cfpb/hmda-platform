
package hmda.validation

import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.filing.ts.{TransmittalLar, TransmittalSheet}

package object engine {
  def selectTsEngine(year: Int): ValidationEngine[TransmittalSheet] =
    year match {
      case 2018 => TsEngine2018
      case 2019 => TsEngine2019
      case _ =>
        TsEngine2019 // TODO: determine what engine to pick if the user enters a year that is not covered
    }

  def selectTsLarEngine(year: Int): ValidationEngine[TransmittalLar] =
    year match {
      case 2018 => TsLarEngine2018
      case 2019 => TsLarEngine2019
      case _ =>
        TsLarEngine2019 // TODO: determine what engine to pick if the user enters a year that is not covered
    }

  def selectLarEngine(year: Int): ValidationEngine[LoanApplicationRegister] =
    year match {
      case 2018 => LarEngine2018
      case 2019 => LarEngine2019
      case _ =>
        LarEngine2019 // TODO: determine what engine to pick if the user enters a year that is not covered
    }
}