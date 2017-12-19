package hmda.census.model

import com.github.tototoshi.csv.CSVParser._
import hmda.model.fi.lar.LoanApplicationRegister

import scala.util.Try

// The file contains a list of all census tracts and what county and state they are in
// site: https://www.ffiec.gov/censusapp.htm
// file: FFIEC Census flat file for the 2017 HMDA collection year

object TractLookup extends CbsaResourceUtils {
  val values: Set[Tract] = {
    val lines = resourceLines("/ffiec_census_fields.txt", "ISO-8859-1")

    lines.drop(1).map { line =>
      val values = parse(line, '\\', '|', '"').getOrElse(List())
      val msa = values(1)
      val stateFips2010 = values(2)
      val countyFips2010 = values(3)
      val tractFips2010 = values(4)
      val minorityPopulation = Try(values(7).toDouble).getOrElse(0.0)
      val tractMfiToMsaPercent = Try(values(11).toDouble).getOrElse(100.0)

      Tract(
        stateFips2010,
        countyFips2010,
        tractFips2010,
        tractFips2010.slice(0, 4) + "." + tractFips2010.slice(4, 6),
        stateFips2010 + countyFips2010,
        msa,
        minorityPopulation,
        tractMfiToMsaPercent
      )
    }.toSet
  }

  def forLar(lar: LoanApplicationRegister, possibleValues: Set[Tract] = values): Option[Tract] = {
    val geo = lar.geography
    possibleValues.find { t =>
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
  tractMfiPercentageOfMsaMfi: Double = 0.0
)
