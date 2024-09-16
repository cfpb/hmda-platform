package hmda.census.records

import com.typesafe.config.ConfigFactory
import hmda.model.ResourceUtils._
import hmda.model.census.Census

object CensusRecords {

  def parseCensusFile(censusFileName: String): List[Census] = {
    val lines = fileLines(s"/$censusFileName")
    lines
      .drop(1)
      .map { s =>
        val values = s.split("\\|", -1).map(_.trim).toList
        Census(
          collectionYear = values.head.toInt,
          msaMd = if (values(1).isEmpty) 0 else values(1).toInt,
          state = values(2),
          county = values(3),
          tract = values(4),
          medianIncome = if (values(5).isEmpty) 0 else values(5).toInt,
          population = if (values(6).isEmpty) 0 else values(6).toInt,
          minorityPopulationPercent = if (values(7).isEmpty) 0.0 else values(7).toDouble,
          occupiedUnits = if (values(8).isEmpty) 0 else values(8).toInt,
          oneToFourFamilyUnits = if (values(9).isEmpty) 0 else values(9).toInt,
          tractMfi = if (values(10).isEmpty) 0 else values(10).toInt,
          tracttoMsaIncomePercent = if (values(11).isEmpty) 0.0 else values(11).toDouble,
          medianAge = if (values(12).isEmpty) -1 else values(12).toInt,
          smallCounty = if (values(13).nonEmpty && values(13) == "S") true else false,
          name = values(14)
        )
      }
      .toList
  }

  private val config = ConfigFactory.load()

  private val censusFileName2018 = config.getString("hmda.census.fields.2018.filename")
  private val censusFileName2019 = config.getString("hmda.census.fields.2019.filename")
  private val censusFileName2020 = config.getString("hmda.census.fields.2020.filename")
  private val censusFileName2021 = config.getString("hmda.census.fields.2021.filename")
  private val censusFileName2022 = config.getString("hmda.census.fields.2022.filename")
  private val censusFileName2023 = config.getString("hmda.census.fields.2023.filename")
  private val censusFileName2024 = config.getString("hmda.census.fields.2024.filename")
  private val censusFileName2025 = config.getString("hmda.census.fields.2025.filename")


  val (
    indexedTract2018: Map[String, Census],
    indexedCounty2018: Map[String, Census],
    indexedSmallCounty2018: Map[String, Census]
    ) = getCensus(censusFileName2018)

  val (
    indexedTract2019: Map[String, Census],
    indexedCounty2019: Map[String, Census],
    indexedSmallCounty2019: Map[String, Census]
    ) = getCensus(censusFileName2019)


  val (
    indexedTract2020: Map[String, Census],
    indexedCounty2020: Map[String, Census],
    indexedSmallCounty2020: Map[String, Census]
    ) = getCensus(censusFileName2020)

  val (
    indexedTract2021: Map[String, Census],
    indexedCounty2021: Map[String, Census],
    indexedSmallCounty2021: Map[String, Census]
    ) = getCensus(censusFileName2021)

  val (
    indexedTract2022: Map[String, Census],
    indexedCounty2022: Map[String, Census],
    indexedSmallCounty2022: Map[String, Census]
    ) = getCensus(censusFileName2022)

  val (
    indexedTract2023: Map[String, Census],
    indexedCounty2023: Map[String, Census],
    indexedSmallCounty2023: Map[String, Census]
    ) = getCensus(censusFileName2023)

  val (
    indexedTract2024: Map[String, Census],
    indexedCounty2024: Map[String, Census],
    indexedSmallCounty2024: Map[String, Census]
    ) = getCensus(censusFileName2024)
  
  val (
    indexedTract2025: Map[String, Census],
    indexedCounty2025: Map[String, Census],
    indexedSmallCounty2025: Map[String, Census]
    ) = getCensus(censusFileName2025)

  def yearTractMap(year: Int): Map[String, Census] = {
    year match {
      case 2018 =>
        indexedTract2018
      case 2019 =>
        indexedTract2019
      case 2020 =>
        indexedTract2020
      case 2021 =>
        indexedTract2021
      case 2022 =>
        indexedTract2022
      case 2023 =>
        indexedTract2023
      case 2024 =>
        indexedTract2024
      case 2025 =>
        indexedTract2025
      case _ =>
        indexedTract2024
    }
  }

  def yearCountyMap(year: Int): Map[String, Census] = {
    year match {
      case 2018 =>
        indexedCounty2018
      case 2019 =>
        indexedCounty2019
      case 2020 =>
        indexedCounty2020
      case 2021 =>
        indexedCounty2021
      case 2022 =>
        indexedCounty2022
      case 2023 =>
        indexedCounty2023
      case 2024 =>
        indexedCounty2024
      case 2025 =>
        indexedCounty2025
      case _ =>
        indexedCounty2024
    }
  }

  def getCensusOnTractandCounty(tract: String, county: String, year: Int): Census = {
    val tractMap = yearTractMap(year)
    tractMap.getOrElse(tract, getCensusFromCounty(county, year))
  }

  private def getCensusFromCounty(county: String, year: Int): Census = {
    val countyMap = yearCountyMap(year)
    val countyCensus = countyMap.getOrElse(county, Census())
    Census(
      msaMd = countyCensus.msaMd,
      state = countyCensus.state,
      county = countyCensus.county,
      medianIncome = countyCensus.medianIncome,
      smallCounty = countyCensus.smallCounty,
      name = countyCensus.name
    )
  }

  private def getCensus(fileName: String): (Map[String, Census], Map[String, Census], Map[String, Census]) =
    parseCensusFile(fileName).foldLeft((Map[String, Census](), Map[String, Census](), Map[String, Census]())) {
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
