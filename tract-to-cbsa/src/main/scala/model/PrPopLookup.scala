package model

import hmda.model.ResourceUtils

object PrPopLookup extends ResourceUtils with CbsaResourceUtils {
  val values: Seq[PrPopulation] = {
    val lines = resourceLines("/PRM-EST00INT-AGESEX-5YR.csv")

    lines.drop(1).map { line =>
      val values = line.split(',').map(_.trim)
      val sumlev = values(0)
      val countyFips = values(1)
      val municipioName = values(2)
      val sex = values(3)
      val ageGrp = values(4)
      val popBase2000 = values(5).toInt
      val popEst2000 = values(6).toInt
      val popEst2001 = values(7).toInt
      val popEst2002 = values(8).toInt
      val popEst2003 = values(9).toInt
      val popEst2004 = values(10).toInt
      val popEst2005 = values(11).toInt
      val popEst2006 = values(12).toInt
      val popEst2007 = values(13).toInt
      val popEst2008 = values(14).toInt
      val popEst2009 = values(15).toInt
      val popEst2010 = values(16).toInt

      PrPopulation(
        "72" + countyFips,
        smallCountyChecker(popBase2000),
        sex,
        ageGrp
      )
    }.filter(pop => pop.sex == "0" && pop.ageGroup == "0").toSeq
  }
}
