package hmda.model.census

case class CountyLoanLimit(
    stateCode: String = "",
    countyCode: Int = 0,
    countyName: String = "",
    stateName: String = "",
    cbsa: Int = 0,
    oneUnitLimit: Int = 0,
    twoUnitLimit: Int = 0,
    threeUnitLimit: Int = 0,
    fourUnitLimit: Int = 0
)
