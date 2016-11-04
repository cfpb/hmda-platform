package model

// The file contains a list of all census tracts and what county and state they are in
// site: https://www.census.gov/geo/maps-data/data/relationship.html
// file: http://www2.census.gov/geo/docs/maps-data/data/rel/trf_txt/us2010trf.txt

object TractLookup extends CbsaResourceUtils {
  val values: Set[Tract] = {
    val lines = csvLines("/us2010trf.txt")

    lines.map { values =>
      val stateFips2010 = values(9)
      val countyFips2010 = values(10)
      val tractFips2010 = values(11)

      Tract(
        stateFips2010,
        countyFips2010,
        tractFips2010,
        tractFips2010.slice(0, 4) + "." + tractFips2010.slice(4, 6),
        stateFips2010 + countyFips2010
      )
    }.toSet
  }
}

case class Tract(
  state: String = "",
  county: String = "",
  tract: String = "",
  tractDec: String = "",
  key: String = ""
)

