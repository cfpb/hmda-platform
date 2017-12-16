package hmda.model.census

import hmda.model.ResourceUtils
import hmda.model.fi.lar.Geography

import scala.util.Try

object CBSATractLookup extends ResourceUtils {
  val values: Seq[CBSATract] = {
    val lines = resourceLines("/tract_to_cbsa_2015.txt")

    lines.map { line =>
      val values = line.split('|').map(_.trim)
      val name = values(0)
      val metDivName = values(1)
      val state = values(2)
      val countyFips = values(3)
      val county = values(4)
      val tracts = values(5)
      val geoIdMsa = values(6)
      val metDivFp = values(7)
      val smallCounty = Try(values(8).toInt).getOrElse(0)
      val stateCode = values(9)
      val tractDecimal = values(10)

      CBSATract(
        name,
        metDivName,
        state,
        countyFips,
        county,
        tracts,
        geoIdMsa,
        metDivFp,
        smallCounty,
        stateCode,
        tractDecimal
      )
    }.toSeq
  }

  val smallCounties =
    values.filter { cbsa => cbsa.smallCounty == 1 }
      .map { cbsa => (cbsa.state, cbsa.county) }
      .toSet

  def geoIsSmallCounty(geo: Geography): Boolean = smallCounties.contains((geo.state, geo.county))

}

case class CBSATract(
  name: String,
  metDivName: String,
  state: String,
  countyFips: String,
  county: String,
  tracts: String,
  geoIdMsa: String,
  metDivFp: String,
  smallCounty: Int,
  stateCode: String,
  tractDecimal: String
)
