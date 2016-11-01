package model

// This file contains data on the population of counties in the states
// site: http://www.census.gov/popest/data/intercensal/county/county2010.html
// file path: http://www.census.gov/popest/data/intercensal/county/files/CO-EST00INT-TOT.csv

object StatesPopLookup extends CbsaResourceUtils {
  val values: Seq[Population] = {
    val lines = resourceLinesIso("/2000-2010_pop_estimates.csv")

    lines.drop(1).map { line =>
      val values = line.split(',').map(_.trim)
      val sumlev = values(0)
      val region = values(1)
      val division = values(2)
      val stateFips = values(3)
      val countyFips = values(4)
      val stateName = values(5)
      val cityname = values(6)
      val popBase2000 = values(7).toInt
      val popEst2000 = values(9).toInt
      val popEst2001 = values(10).toInt
      val popEst2002 = values(11).toInt
      val popEst2003 = values(12).toInt
      val popEst2004 = values(13).toInt
      val popEst2005 = values(14).toInt
      val popEst2006 = values(15).toInt
      val popEst2007 = values(16).toInt
      val popEst2008 = values(17).toInt
      val popEst2009 = values(18).toInt
      val popEst2010 = values(19).toInt

      StatesPopulation(
        stateFips + countyFips,
        smallCountyChecker(popBase2000)
      )
    }.toSeq
  }
}
