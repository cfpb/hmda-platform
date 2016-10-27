package model

import hmda.model.ResourceUtils

object CbsaLookup extends ResourceUtils {
  val values: Seq[Cbsa] = {
    val lines = resourceLines("/state.csv")

    lines.map { line =>
      val values = line.split(',').map(_.trim)
      val cbsaCode 			= values(0)
      val metroDivCode 	= values(1)
      val csaCode 			= values(2)
      val cbsaTitle			= values(3)
      val metroOrMicro 	= values(4)
      val metroDivTitle 	= values(5)
      val csaTitle			= values(6)
      val countyName		= values(7)
      val stateName			= values(8)
      val stateFips			= values(9)
      val countyFips		= values(10)
      val centralOutlying	= values(11)

      Cbsa(
        cbsaCode,
        metroDivCode,
        cbsaTitle,
        metroDivTitle,
        stateFips + countyFips
      )
    }.toSeq
  }
}

case class Cbsa(
                  cbsa: String,
                  metroDiv: String,
                  cbsaTitle: String,
                  metroDivTitle: String,
                  key: String
                )
