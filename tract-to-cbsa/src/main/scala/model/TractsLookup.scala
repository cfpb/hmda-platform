package model

import hmda.model.ResourceUtils

object TractLookup extends ResourceUtils with CbsaResourceUtils {
  val values: Set[Tract] = {
    val lines = resourceLinesIso("/us2010trf.txt")

    lines.map { line =>
      val values = line.split(',').map(_.trim)
      val countyFips2000 = values(0)
      val tractFips2000 = values(1)
      val stateFips2010 = values(9)
      val countyFips2010 = values(10)
      val tractFips2010 = values(11)

      Tract(
        stateFips2010,
        countyFips2010,
        tractFips2010,
        tractFips2010,
        stateFips2010 + countyFips2000
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
