package hmda.model.census

import spray.json.{ DefaultJsonProtocol, JsonFormat }

object Census {
  val states: Map[String, State] = Map(
    "AL" -> State("01", "Alabama"),
    "AK" -> State("02", "Alaska"),
    "AZ" -> State("04", "Arizona"),
    "AR" -> State("05", "Arkansas"),
    "CA" -> State("06", "California"),
    "CO" -> State("08", "Colorado"),
    "CT" -> State("09", "Connecticut"),
    "DE" -> State("10", "Delaware"),
    "FL" -> State("12", "Florida"),
    "GA" -> State("13", "Georgia"),
    "HI" -> State("15", "Hawaii"),
    "ID" -> State("16", "Idaho"),
    "IL" -> State("17", "Illinois"),
    "IN" -> State("18", "Indiana"),
    "IA" -> State("19", "Iowa"),
    "KS" -> State("20", "Kansas"),
    "KY" -> State("21", "Kentucky"),
    "LA" -> State("22", "Louisiana"),
    "ME" -> State("23", "Maine"),
    "MD" -> State("24", "Maryland"),
    "MA" -> State("25", "Massachusetts"),
    "MI" -> State("26", "Michigan"),
    "MN" -> State("27", "Minnesota"),
    "MS" -> State("28", "Mississippi"),
    "MO" -> State("29", "Missouri"),
    "MT" -> State("30", "Montana"),
    "NE" -> State("31", "Nebraska"),
    "NV" -> State("32", "Nevada"),
    "NH" -> State("33", "New Hampshire"),
    "NJ" -> State("34", "New Jersey"),
    "NM" -> State("35", "New Mexico"),
    "NY" -> State("36", "New York"),
    "NC" -> State("37", "North Carolina"),
    "ND" -> State("38", "North Dakota"),
    "OH" -> State("39", "Ohio"),
    "OK" -> State("40", "Oklahoma"),
    "OR" -> State("41", "Oregon"),
    "PA" -> State("42", "Pennsylvania"),
    "RI" -> State("44", "Rhode Island"),
    "SC" -> State("45", "South Carolina"),
    "SD" -> State("46", "South Dakota"),
    "TN" -> State("47", "Tennessee"),
    "TX" -> State("48", "Texas"),
    "UT" -> State("49", "Utah"),
    "VT" -> State("50", "Vermont"),
    "VA" -> State("51", "Virginia"),
    "WA" -> State("53", "Washington"),
    "WV" -> State("54", "West Virginia"),
    "WI" -> State("55", "Wisconsin"),
    "WY" -> State("56", "Wyoming"),
    "AS" -> State("60", "American Samoa"),
    "DC" -> State("11", "District of Columbia"),
    "FM" -> State("64", "Federated States of Micronesia"),
    "GU" -> State("66", "Guam"),
    "MH" -> State("68", "Marshall Islands"),
    "MP" -> State("69", "Northern Mariana Islands"),
    "PW" -> State("70", "Palau"),
    "PR" -> State("72", "Puerto Rico"),
    "VI" -> State("78", "Virgin Islands")
  )

  // Spray support for encoding and decoding as JSON
  import DefaultJsonProtocol._
  implicit val censusJsonFormat: JsonFormat[Census] = jsonFormat16(Census.apply)
}

case class Census(
  id: Int = 0,
  collectionYear: Int = 0,
  msaMd: Int = 99999,
  state: String = "",
  county: String = "",
  tract: String = "",
  medianIncome: Int = 0,
  population: Int = 0,
  minorityPopulationPercent: Double = 0F,
  occupiedUnits: Int = 0,
  oneToFourFamilyUnits: Int = 0,
  tractMfi: Int = 0,
  tracttoMsaIncomePercent: Double = 0F,
  medianAge: Int = 0,
  smallCounty: Boolean = false,
  name: String = ""
) {
  def toHmdaTract: String  = s"${state}${county}${tract}"
  def toHmdaCounty: String = s"${state}${county}"
}
