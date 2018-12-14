package hmda.census.validation

import hmda.model.census.Census

object CensusValidation {

  def isTractValid(tract: String,
                   indexedTract: Map[String, Census]): Boolean = {
    indexedTract.contains(tract)
  }

  def isCountyValid(county: String,
                    indexedCounty: Map[String, Census]): Boolean = {
    indexedCounty.contains(county)
  }

  def isPopulationGt30k(county: String,
                        indexedCounties: Map[String, Census]): Boolean = {
    indexedCounties.contains(county)
  }

}
