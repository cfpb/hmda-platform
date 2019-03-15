package hmda.census.records

import com.typesafe.config.ConfigFactory
import hmda.model.ResourceUtils._
import hmda.model.census.CountyLoanLimit

object CountyLoanLimitRecords {

  def parseCountyLoanLimitFile(): List[CountyLoanLimit] = {
    val config = ConfigFactory.load()
    val countyLoanLimitFileName =
      config.getString("hmda.countyLoanLimit.fields.filename")
    val lines = fileLines(s"/$countyLoanLimitFileName")
    lines
      .drop(1)
      .map { s =>
        val values = s.split("\\|", -1).map(_.trim).toList
        CountyLoanLimit(
          stateCode = values(0),
          countyCode = values(1).toInt,
          countyName = values(2),
          stateName = values(3),
          cbsa = values(4).toInt,
          oneUnitLimit = values(5).toInt,
          twoUnitLimit = values(6).toInt,
          threeUnitLimit = values(7).toInt,
          fourUnitLimit = values(8).toInt
        )
      }
      .toList
  }
}
