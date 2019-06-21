package hmda.validation

import hmda.model.filing._


package object engine {
  def selectTsEngine(year: Int): Either[ValidationEngine[ts._2018.TransmittalSheet],ValidationEngine[ts._2019.TransmittalSheet]] =
    year match {
      case 2018 => Left(TsEngine2018)
      case 2019 => Right(TsEngine2019)
      case _ =>
        Right(TsEngine2019) // TODO: determine what engine to pick if the user enters a year that is not covered
    }

  def selectTsLarEngine(year: Int): Either[ValidationEngine[ts._2018.TransmittalLar],ValidationEngine[ts._2019.TransmittalLar]] =
    year match {
      case 2018 => Left(TsLarEngine2018)
      case 2019 => Right(TsLarEngine2019)
      case _ =>
        Right(TsLarEngine2019) // TODO: determine what engine to pick if the user enters a year that is not covered
    }

  def selectLarEngine(year: Int): Either[ValidationEngine[lar._2018.LoanApplicationRegister],ValidationEngine[lar._2019.LoanApplicationRegister] ] =
    year match {
      case 2018 => Left(LarEngine2018)
      case 2019 => Right(LarEngine2019)
      case _ =>
        Right(LarEngine2019) // TODO: determine what engine to pick if the user enters a year that is not covered
    }
}
