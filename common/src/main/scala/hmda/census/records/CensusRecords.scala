package hmda.census.records

import com.typesafe.config.ConfigFactory
import hmda.model.ResourceUtils._
import hmda.model.census.Census

object CensusRecords {

  def parseCensusFile: List[Census] = {
    val config = ConfigFactory.load()
    val censusFileName =
      config.getString("hmda.census.fields.filename")
    val lines = fileLines(s"/$censusFileName")
    lines
      .drop(1)
      .map { s =>
        val values = s.split("\\|", -1).map(_.trim).toList
        Census(
          collectionYear = values(0).toInt,
          msaMd = values(1).toInt,
          state = values(2),
          county = values(3),
          tract = values(4),
          medianIncome = values(5).toInt,
          population = values(6).toInt,
          minorityPopulationPercent =
            if (values(7).isEmpty) 0.0 else values(7).toDouble,
          occupiedUnits = if (values(8).isEmpty) 0 else values(8).toInt,
          oneToFourFamilyUnits = if (values(9).isEmpty) 0 else values(9).toInt,
          tractMfi = if (values(10).isEmpty) 0 else values(10).toInt,
          tracttoMsaIncomePercent =
            if (values(11).isEmpty) 0.0 else values(11).toDouble,
          medianAge = if (values(12).isEmpty) 0 else values(12).toInt,
          smallCounty =
            if (!values(13).isEmpty && values(13) == "S") true else false,
          name = values(14)
        )
      }
      .toList
  }

  val (indexedTract: Map[String, Census],
       indexedCounty: Map[String, Census],
       indexedSmallCounty: Map[String, Census]) =
    parseCensusFile.foldLeft(
      (Map[String, Census](), Map[String, Census](), Map[String, Census]())) {
      case ((m1, m2, m3), c) =>
        (
          m1 + (c.toHmdaTract -> c),
          m2 + (c.toHmdaCounty -> c),
          if (c.smallCounty)
            m3 + (c.toHmdaCounty -> c)
          else m3
        )
    }

}
