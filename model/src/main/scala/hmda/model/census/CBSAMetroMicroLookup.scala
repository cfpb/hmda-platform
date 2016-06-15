package hmda.model.census

import java.io.File

import scala.io.Source

object CBSAMetroMicroLookup {
  val values: Seq[CBSAMetroMicro] = {
    val lines = Source.fromFile(new File("model/src/main/resources/tl_2013_us_cbsa.csv")).getLines
    lines.map { line =>
      val values = line.split('|').map(_.trim)
      val combinedCode = values(0)
      val metroOrMicroCode = values(1)
      val geoId = values(2)
      val name = values(3)
      val legalStatname = values(4)
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
        legalStatname,
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
  legalStatname: String,
  legalStatMetroMicro: String,
  metroMicro: Int,
  metroMicroStatArea: String,
  areaLand: String,
  areaWater: String,
  internalPointLat: String,
  internalPointLon: String
)
