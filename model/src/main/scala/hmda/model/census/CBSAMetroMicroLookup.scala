package hmda.model.census

import hmda.model.ResourceUtils

object CBSAMetroMicroLookup extends ResourceUtils {
  val values: Seq[CBSAMetroMicro] = {
    val lines = resourceLines("/tl_2013_us_cbsa.csv")

    lines.map { line =>
      val values = line.split('|').map(_.trim)
      val combinedCode = values(0)
      val metroOrMicroCode = values(1)
      val geoId = values(2)
      val name = values(3)
      val legalStatName = values(4)
      val legalStatMetroMicro = values(5)
      val metroMicro = values(6).toInt
      val metroMicroStatArea = values(7)
      val areaLand = values(8)
      val areaWater = values(9)
      val internalPointLat = values(10)
      val internalPointLon = values(11)

      CBSAMetroMicro(
        combinedCode,
        metroOrMicroCode,
        geoId,
        name,
        legalStatName,
        legalStatMetroMicro,
        metroMicro,
        metroMicroStatArea,
        areaLand,
        areaWater,
        internalPointLat,
        internalPointLon
      )
    }.toSeq
  }
}

case class CBSAMetroMicro(
  combinedCode: String,
  metroOrMicroCode: String,
  geoId: String,
  name: String,
  legalStatName: String,
  legalStatMetroMicro: String,
  metroMicro: Int,
  metroMicroStatArea: String,
  areaLand: String,
  areaWater: String,
  internalPointLat: String,
  internalPointLon: String
)
