package hmda.census.model

object CensusModel {

  case class TractCheck(tract: String)
  case class CountyCheck(county: String)
  case class TractValidated(isValid: Boolean)
}
