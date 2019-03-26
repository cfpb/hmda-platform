package hmda.model.census

case class CountyLoanLimit(
    stateCode: String = "",
    countyCode: String = "",
    countyName: String = "",
    stateName: String = "",
    cbsa: String = "",
    oneUnitLimit: Int = 0,
    twoUnitLimit: Int = 0,
    threeUnitLimit: Int = 0,
    fourUnitLimit: Int = 0
)
