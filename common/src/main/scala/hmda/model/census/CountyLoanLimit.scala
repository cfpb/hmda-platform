package hmda.model.census

object CountyLoanLimit {
    val countyLoanLimits: Seq[CountyLoanLimit] = {
        val 
    }
}

case class CountyLoanLimit (
    stateCode: Int = 0,
    countyCode: Int = 0,
    countyName: String = "",
    stateName: String = "",
    cbsa: Int = 0,
    oneUnitLimit: Int = 0,
    twoUnitLimt: Int = 0,
    threeUnitLimit: Int = 0,
    fourUnitLimit: Int = 0
)
