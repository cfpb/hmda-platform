package model

import com.github.tototoshi.csv.CSVParser.parse

// This is 2015 data as 2017 data is not yet published
// This file contains information about metro and micropolitan statisical areas
// site: http://www.census.gov/population/metro/data/def.html
// file path: http://www.census.gov/population/metro/files/lists/2015/List1.xls

object CbsaLookup extends CbsaResourceUtils {
  val values: Seq[Cbsa] = {
    val lines = resourceLinesIso("/jul_2015_cbsa.csv")
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
}

case class Cbsa(
  cbsa: String = "",
  metroDiv: String = "",
  cbsaTitle: String = "",
  metroDivTitle: String = "",
  key: String = ""
)
