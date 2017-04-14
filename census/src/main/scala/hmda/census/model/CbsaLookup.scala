package hmda.census.model

import com.github.tototoshi.csv.CSVParser.parse

// This is 2015 data as 2017 data is not yet published
// This file contains information about metro and micropolitan statistical areas
// site: http://www.census.gov/population/metro/data/def.html
// file path: http://www.census.gov/population/metro/files/lists/2015/List1.xls

object CbsaLookup extends CbsaResourceUtils {
  val values: Seq[Cbsa] = {
    val lines = resourceLines("/jul_2015_cbsa.csv", "ISO-8859-1")
    lines.map { line =>
      val values = parse(line, '\\', ',', '"').getOrElse(List())
      val cbsaCode = values(0)
      val metroDivCode = values(1)
      val csaCode = values(2)
      val cbsaTitle = values(3)
      val metroOrMicro = values(4)
      val metroDivTitle = values(5)
      val csaTitle = values(6)
      val countyName = values(7)
      val stateName = values(8)
      val stateFips = values(9)
      val countyFips = values(10)
      val centralOutlying = values(11)
      Cbsa(
        cbsaCode,
        metroDivCode,
        cbsaTitle,
        metroDivTitle,
        stateFips + countyFips
      )
    }
  }.toSeq

  private val cbsaIdToNameMap: Map[String, String] = {
    values.map(x => (x.cbsa, x.cbsaTitle)).toMap
  }

  def nameFor(cbsaId: String): String = {
    cbsaIdToNameMap.getOrElse(cbsaId, "NA")
  }
}

case class Cbsa(
  cbsa: String = "",
  metroDiv: String = "",
  cbsaTitle: String = "",
  metroDivTitle: String = "",
  key: String = ""
)
