package hmda.census.model

// This file contains data on the population of MUNICIPIOs in Puerto Rico
// site: http://www.census.gov/popest/data/intercensal/puerto_rico/pr2010.html
// file path: http://www.census.gov/popest/data/intercensal/puerto_rico/files/PRM-EST00INT-AGESEX-5YR.csv

object PrPopLookup extends CbsaResourceUtils {
  val values: Seq[PrPopulation] = {
    val lines = csvLines("/PRM-EST00INT-AGESEX-5YR.csv")

    lines.drop(1).map { values =>
      val countyFips = leftPad(3, values(1))
      val sex = values(3)
      val ageGrp = values(4)
      val popBase2000 = values(5).toInt

      PrPopulation(
        "72" + countyFips,
        smallCountyChecker(popBase2000),
        sex,
        ageGrp
      )
    }.filter(pop => pop.sex == "0" && pop.ageGroup == "0")
  }
}
