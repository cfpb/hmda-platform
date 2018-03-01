package hmda.census.model

import com.github.tototoshi.csv.CSVParser.parse

// This is 2015 data as 2017 data is not yet published
// This file contains information about metro and micropolitan statistical areas
// site: http://www.census.gov/population/metro/data/def.html
// file path: http://www.census.gov/population/metro/files/lists/2015/List1.xls

object CbsaLookup extends CbsaResourceUtils {
  val values: Seq[Cbsa] = {
    val lines = resourceLines("/jul_2015_cbsa.csv", "ISO-8859-1")
    lines.drop(3).map { line =>
      val values = parse(line, '\\', ',', '"').getOrElse(List())
      val cbsaCode = values(0)
      val metroDivCode = values(1)
      val cbsaTitle = values(3)
      val metroDivTitle = values(5)
      val stateFips = values(9)
      val countyFips = values(10)
      val countyName = values(7)
      Cbsa(
        cbsaCode,
        metroDivCode,
        cbsaTitle,
        metroDivTitle,
        stateFips + countyFips,
        countyName
      )
    }
  }.toSeq

  private val cbsaIdToNameMap: Map[String, String] = {
    values.map(x => (x.cbsa, x.cbsaTitle)).toMap
  }

  private val mdIdToNameMap: Map[String, String] = {
    values.map(c => (c.metroDiv, c.metroDivTitle)).toMap
  }

  // Returns CBSA Title if `id` is a CBSA Code, or
  //   Metropolitan Division Title if `id` is a MD Code.
  def nameFor(id: String): String = {
    val cbsa = cbsaIdToNameMap.get(id)
    val md = mdIdToNameMap.get(id)
    val na = Some("NA")
    Seq(cbsa, md, na).flatten.head
  }
}

case class Cbsa(
  cbsa: String = "",
  metroDiv: String = "",
  cbsaTitle: String = "",
  metroDivTitle: String = "",
  key: String = "",
  countyName: String = ""
)
