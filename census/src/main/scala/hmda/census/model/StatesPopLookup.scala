package hmda.census.model

// This file contains data on the population of counties in the states
// site: http://www.census.gov/popest/data/intercensal/county/county2010.html
// file path: http://www.census.gov/popest/data/intercensal/county/files/CO-EST00INT-TOT.csv

object StatesPopLookup extends CbsaResourceUtils {
  val values: Seq[Population] = {
    val lines = csvLines("/2000-2010_pop_estimates.csv")
    lines.drop(1).map { values =>
      val stateFips = leftPad(2, values(3))
      val countyFips = leftPad(3, values(4))
      val popBase2000 = values(7).toInt

      StatesPopulation(
        stateFips + countyFips,
        smallCountyChecker(popBase2000)
      )
    }
  }
}
