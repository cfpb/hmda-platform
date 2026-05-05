package hmda.dataBrowser.models

import enumeratum.EnumEntry
import hmda.census.records.CensusRecords

sealed trait County extends EnumEntry

object County {
  def validateCounties(counties: Seq[String]): Either[Seq[String], Seq[String]] = {
    val potentialCounties: Seq[String] = counties.map(county => county)
    val validCounties                  = CensusRecords.indexedCounty2018

    val isValidCounty: Boolean = potentialCounties.forall(validCounties.contains(_))

    if (isValidCounty) Right(potentialCounties)
    else {
      Left(potentialCounties.collect {
        case (input) => input
      })
    }

  }
}

case class Count2017(county: String) extends AnyVal