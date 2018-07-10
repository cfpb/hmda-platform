package hmda.census.model

import com.github.tototoshi.csv.CSVParser._
import hmda.model.fi.lar.LoanApplicationRegister

import scala.util.{ Success, Try }

// The file contains a list of all census tracts and what county and state they are in
// site: https://www.ffiec.gov/censusapp.htm
// file: FFIEC Census flat file for the 2017 HMDA collection year

object TractLookup extends CbsaResourceUtils {
  val valuesExtended: Set[TractExtended] = {
    val lines = resourceLines("/ffiec_census_fields.txt", "ISO-8859-1")

    lines.drop(1).map { line =>
      val values = parse(line, '\\', '|', '"').getOrElse(List())
      val msa = values(1)
      val stateFips2010 = values(2)
      val countyFips2010 = values(3)
      val tractFips2010 = values(4)
      val ffiecMfi = Try(values(5).toInt).getOrElse(0)
      val totalPersons = Try(values(6).toInt).getOrElse(0)
      val minorityPopulation = Try(values(7).toDouble).getOrElse(0.0)
      val ownerOccupied = Try(values(8).toInt).getOrElse(0)
      val oneToFour = Try(values(9).toInt).getOrElse(0)
      val tractMfiToMsaPercent = Try(values(11).toDouble).getOrElse(100.0)
      val medianYearHomesBuilt = Try(values(12).toInt) match {
        case Success(value) => Some(2015 - value)
        case _ => None
      }

      TractExtended(
        stateFips2010,
        countyFips2010,
        tractFips2010,
        tractFips2010.slice(0, 4) + "." + tractFips2010.slice(4, 6),
        stateFips2010 + countyFips2010,
        msa,
        ffiecMfi,
        totalPersons,
        ownerOccupied,
        oneToFour,
        minorityPopulation,
        tractMfiToMsaPercent,
        medianYearHomesBuilt
      )
    }.toSet
  }
  val values: Set[Tract] = valuesExtended.map(_.toTract)

  def forLar(lar: LoanApplicationRegister, possibleValues: Set[Tract] = values): Option[Tract] = {
    val geo = lar.geography
    possibleValues.find { t =>
      t.tractDec == geo.tract &&
        t.state == geo.state &&
        t.county == geo.county
    }
  }

  def forLarExtended(lar: LoanApplicationRegister): Option[TractExtended] = {
    val geo = lar.geography
    valuesExtended.find { t =>
      t.tractDec == geo.tract &&
        t.state == geo.state &&
        t.county == geo.county
    }
  }
}

case class Tract(
  state: String = "",
  county: String = "",
  tract: String = "",
  tractDec: String = "",
  key: String = "",
  msa: String = "",
  minorityPopulationPercent: Double = 0.0,
  tractMfiPercentageOfMsaMfi: Double = 0.0,
  medianYearHomesBuilt: Option[Int] = None
)

case class TractExtended(
    state: String = "",
    county: String = "",
    tract: String = "",
    tractDec: String = "",
    key: String = "",
    msa: String = "",
    ffiecMfi: Int = 0,
    totalPersons: Int = 0,
    ownerOccupied: Int = 0,
    oneToFourUnits: Int = 0,
    minorityPopulationPercent: Double = 0.0,
    tractMfiPercentageOfMsaMfi: Double = 0.0,
    medianYearHomesBuilt: Option[Int] = None
) {
  def toTract = Tract(state, county, tract,
    tractDec, key, msa, minorityPopulationPercent,
    tractMfiPercentageOfMsaMfi, medianYearHomesBuilt)
  def toCSV = {
    val mPPString = BigDecimal(minorityPopulationPercent).setScale(2, BigDecimal.RoundingMode.HALF_UP).toDouble
    val tMfiPString = BigDecimal(tractMfiPercentageOfMsaMfi).setScale(2, BigDecimal.RoundingMode.HALF_UP).toDouble
    s"$totalPersons|$mPPString|$ffiecMfi|$tMfiPString|$ownerOccupied|$oneToFourUnits"
  }
}
