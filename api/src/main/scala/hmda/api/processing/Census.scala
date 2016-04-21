package hmda.api.processing

trait Census {

  /*
  Returns the full name of a Metropolitan Statistical Authority (MSA)
  activityYear: The year for which the HMDA data is being collected
  msaCode: Code to identify the Metropolitan Statistical Authority/Metropolitan Division
   */
  def getMSAName(activityYear: String, msaCode: String): String

  /*
  Checks whether or not the FIPS state and county codes, and Census CBSA tract number are a valid combination
  activityYear: The year for which the HMDA data is being collected
  state: 2-digit FIPS code that identifies the state
  county: 3-digit FIPS code that identifies the county
  tract: 6-digit census tract number
   */
  def isValidCensusCombination(
    activityYear: String,
    state: String,
    county: String,
    tract: String
  ): Boolean

  /*
  Checks whether or not the FIPS state and county codes, Metropolitan Statistical Area/Metropolitan division code and Census CBSA tract number are a valid combination
  activityYear: The year for which the HMDA data is being collected
  state: 2-digit FIPS code that identifies the state
  county: 3-digit FIPS code that identifies the county
  metroArea: 5-digit code to identify the Metropolitan Statistical Authority/Metropolitan Division
  tract: 6-digit census tract number
   */

  def isValidCensusInMSA(
    activityYear: String,
    state: String,
    county: String,
    metroArea: String,
    tract: String
  ): Boolean

  /*
  Checks whether or not the Metropolitan Statistical Area/Metropolitan Division code given is valid
  activityYear: The year for which the HMDA data is being collected
  state: 2-digit FIPS code that identifies the state
   */
  def isValidMSA(activityYear: String, msa: String): Boolean

  /*
  Checks whether or not the FIPS state and county codes are a valid combination
  activityYear: The year for which the HMDA data is being collected
  state: 2-digit FIPS code that identifies the state
  county: 3-digit FIPS code that identifies the county
   */
  def isValidMSAStateCounty(activityYear: String, state: String, county: String): Boolean
}
