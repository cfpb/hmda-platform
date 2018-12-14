package hmda.census.records

import com.typesafe.config.ConfigFactory
import hmda.model.census.Census
import hmda.model.ResourceUtils._

trait CensusRecords {

  val config = ConfigFactory.load()
  val censusFileName =
    config.getString("hmda.census.fields.filename")

  def parseCensusFile: List[Census] = {
    val lines = fileLines(s"/$censusFileName")
    lines
      .drop(1)
      .map { s =>
        val values = s.split("\\|", -1).map(_.trim).toList
        Census(
          id = values(0).toInt,
          collectionYear = values(1).toInt,
          msaMd = values(2).toInt,
          state = values(3),
          county = values(4),
          tract = values(5),
          medianIncome = values(6).toInt,
          population = values(7).toInt,
          minorityPopulationPercent =
            if (values(8).isEmpty) 0.0 else values(8).toDouble,
          occupiedUnits = if (values(9).isEmpty) 0 else values(9).toInt,
          oneToFourFamilyUnits =
            if (values(10).isEmpty) 0 else values(10).toInt,
          tractMfi = if (values(11).isEmpty) 0 else values(11).toInt,
          tracttoMsaIncomePercent =
            if (values(12).isEmpty) 0.0 else values(12).toDouble,
          medianAge = if (values(13).isEmpty) 0 else values(13).toInt,
          smallCounty =
            if (!values(14).isEmpty && values(14) == "S") true else false
        )
      }
      .toList
  }

  val populationSizeDeterminator = 30000

  val (indexedTract, indexedCounty, indexedLargeCounty) =
    parseCensusFile.foldLeft(
      (Map[String, Census](), Map[String, Census](), Map[String, Census]())) {
      case ((m1, m2, m3), c) =>
        (
          m1 + (c.toHmdaTract -> c),
          m2 + (c.toHmdaCounty -> c),
          if (!c.smallCounty && c.population > populationSizeDeterminator)
            m3 + (c.toHmdaCounty -> c)
          else m3
        )
    }

}
