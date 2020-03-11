package hmda.census.records

import hmda.model.ResourceUtils._
import hmda.model.census.CountyLoanLimit
import hmda.parser.derivedFields.StateBoundries

object CountyLoanLimitRecords {

  def parseCountyLoanLimitFile(fileName: String): List[CountyLoanLimit] = {
    val lines = fileLines(s"/$fileName")
    lines
      .drop(1)
      .map { s =>
        val values = s.split("\\|", -1).map(_.trim).toList
        CountyLoanLimit(
          stateCode = values(0),
          countyCode = values(1),
          countyName = values(2),
          stateAbbrv = values(3),
          cbsa = values(4),
          oneUnitLimit = values(5).toInt,
          twoUnitLimit = values(6).toInt,
          threeUnitLimit = values(7).toInt,
          fourUnitLimit = values(8).toInt
        )
      }
      .toList
  }

  def countyLoansLimitByCounty(countyLoanLimits: Seq[CountyLoanLimit]): Map[String, CountyLoanLimit] = {
    countyLoanLimits
      .map(county => county.stateCode + county.countyCode -> county)
      .toMap
  }

  def countyLoansLimitByState(countyLoanLimits: Seq[CountyLoanLimit]): Map[String, StateBoundries] = {
    countyLoanLimits.groupBy(county => county.stateAbbrv).mapValues { countyList =>
      val oneUnit   = countyList.map(county => county.oneUnitLimit)
      val twoUnit   = countyList.map(county => county.twoUnitLimit)
      val threeUnit = countyList.map(county => county.threeUnitLimit)
      val fourUnit  = countyList.map(county => county.fourUnitLimit)
      StateBoundries(
        oneUnitMax = oneUnit.max,
        oneUnitMin = oneUnit.min,
        twoUnitMax = twoUnit.max,
        twoUnitMin = twoUnit.min,
        threeUnitMax = threeUnit.max,
        threeUnitMin = threeUnit.min,
        fourUnitMax = fourUnit.max,
        fourUnitMin = fourUnit.min
      )
    }
  }

}
