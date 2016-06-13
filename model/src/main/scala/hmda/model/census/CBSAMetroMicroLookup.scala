package hmda.model.census

import java.io.File

import scala.io.Source

object CBSAMetroMicroLookup {
  val values: Seq[CBSAMetroMicro] = {
    val lines = Source.fromFile(new File("model/src/main/resources/tl_2013_us_cbsa.csv")).getLines
    lines.map { line =>
      val values = line.split('|').map(_.trim)
      val CSAFP = values(0)
      val CBSAFP = values(1)
      val GEOIOD = values(2)
      val name = values(3)
      val nameLSAD = values(4)
      val LSAD = values(5)
      val MEMI = values(6).toInt
      val MTFCC = values(7)
      val ALAND = values(8)
      val AWATER = values(9)
      val INTPTLAT = values(10)
      val INTPTLON = values(11)

      CBSAMetroMicro(
        CBSAFP,
        GEOIOD,
        name,
        nameLSAD,
        LSAD,
        MEMI,
        MTFCC,
        ALAND,
        AWATER,
        INTPTLAT,
        INTPTLON
      )
    }.toSeq
  }
}

case class CBSAMetroMicro(
  CBSAFP: String,
  GEOIOD: String,
  name: String,
  nameLSAD: String,
  LSAD: String,
  MEMI: Int,
  MTFCC: String,
  ALAND: String,
  AWATER: String,
  INTPTLAT: String,
  INTPTLON: String
)
