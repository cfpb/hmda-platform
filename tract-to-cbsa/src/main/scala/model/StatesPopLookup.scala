package model

import hmda.model.ResourceUtils

object StatesPopLookup extends ResourceUtils with CbsaResourceUtils {
  val values: Seq[Population] = {
    val lines = resourceLines("/2000-2010_pop_estimates.csv")

    lines.map { line =>
      val values = line.split(',').map(_.trim)
      val sumlev = values(0)
      val region = values(1)
      val division = values(2)
      val stateFips = values(3)
      val countyFips = values(4)
      val stateName = values(5)
      val cityname = values(6)
      val popBase2000 = values(7).toInt
      val popEst2000 = values(8).toInt
      val popEst2001 = values(8).toInt
      val popEst2002 = values(8).toInt
      val popEst2003 = values(8).toInt
      val popEst2004 = values(8).toInt
      val popEst2005 = values(8).toInt
      val popEst2006 = values(8).toInt
      val popEst2007 = values(8).toInt
      val popEst2008 = values(8).toInt
      val popEst2009 = values(8).toInt
      val popEst2010 = values(8).toInt



      Population(
        stateFips + countyFips,
          smallCountyChecker(popBase2000)
      )
    }.toSeq
  }
}
