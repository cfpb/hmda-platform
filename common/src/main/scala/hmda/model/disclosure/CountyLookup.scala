package hmda.model.disclosure

import com.github.tototoshi.csv.CSVReader
import com.typesafe.config.ConfigFactory
import hmda.model.ResourceUtils.resource

object CountyLookup {

  case class County(
      cbsaCode: String = "",
      cbsaTitle: String = "",
      countyName: String = "",
      stateName: String = "",
      stateCode: Int = 0,
      countyCode: Int = 0
  )

  val config = ConfigFactory.load()
  val countyFileName = config.getString("hmda.county.countynames")
  val countyList: Seq[County] = {
    val reader = CSVReader.open(resource(s"/$countyFileName", "UTF-8"))
    reader.toStream
      .drop(1)
      .map { field =>
        County(field(0),
               field(3),
               field(7),
               field(8),
               field(9).trim().toInt,
               field(10).trim().toInt)
      }
  }

  val countiesMap =
    countyList.map(e => (s"${e.stateCode}${e.countyCode}", e)).toMap

  def lookupCounty(stateCode: Int, countyCode: Int): County = {
    countiesMap.getOrElse(s"${stateCode}${countyCode}", County())
  }
}
