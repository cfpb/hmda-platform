package hmda.model.census

case class County(
    cbsaCode: String = "",
    cbsaTitle: String = "",
    countyName: String = "",
    stateName: String = "",
    stateCode: Int = 0,
    countyCode: Int = 0
)
