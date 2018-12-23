package hmda.census.validation

import hmda.model.census.Census

object CensusValidation {

  def isTractValid(tract: String,
                   indexedTract: Map[String, Census]): Boolean = {
    indexedTract.contains(tract)
  }

  def isTractValidTest(tract: String): Boolean = {
    true
  }

  def isCountyValid(county: String,
                    indexedCounty: Map[String, Census]): Boolean = {
    indexedCounty.contains(county)
  }

  def isCountySmall(county: String,
                    indexedSmallCounty: Map[String, Census]): Boolean = {
    indexedSmallCounty.contains(county)
  }

}
