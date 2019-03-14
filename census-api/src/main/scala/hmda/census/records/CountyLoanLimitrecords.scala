package hmda.census.records

import com.typesafe.config.ConfigFactory
import hmda.model.ResourceUtils._
import hmda.model.census.Census

object CountyLoanLimitRecords {

  def parseCountyLoanLimitFile: List[Census] = {
    val config = ConfigFactory.load()
    val countyLoanLimitFileName =
      config.getString("hmda.countyLoanLimit.fields.filename")
    val lines = fileLines(s"/$countyLoanLimitFileName")
    lines
      .drop(1)
      .map { s =>
        val values = s.split("\\|", -1).map(_.trim).toList
        CountyLoanLimit(
            stateCode = values(0).toInt,
            countyCode = values(1).toInt,
            countyName = values(2),
            stateName = values(3),
            cbsa = values(4).toInt,
            oneUnitLimit = values(5).toInt,
            twoUnitLimt = values(6).toInt,
            threeUnitLimit = values(7).toInt,
            fourUnitLimit = values(8).toInt
        )
      }
      .toList
  }
}