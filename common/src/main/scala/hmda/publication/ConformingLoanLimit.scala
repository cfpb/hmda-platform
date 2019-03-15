package hmda.publication

import hmda.census.records.CountyLoanLimitRecords
import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.census.CountyLoanLimit

case class LoanLimitInfo(
    totalUnits: Int, 
    loanAmount: Int, 
    lienStatus: LienStatusEnum.
    county: String,
    state: String
    )

case class StateBoundries (
    oneUnitMax: Double,
    oneUnitMin: Double,
    twoUnitMax: Double,
    twoUnitMin: Double,
    threeUnitMax: Double,
    threeUnitMin: Double,
    fourUnitMax: Double,
    fourUnitMin: Double
)

object ConformingLoanLimit {
    
    val countyLoanLimits: Seq[CountyLoanLimit] = parseCountyLoanLimitFile
    val countyLoanLimitsByCounty: Map[String,CountyLoanLimit] = 
        countyLoanLimits.map(county => Map(county.stateCode + county.countyCode -> county))
    val countyLoanLimitsByState =
        countyLoanLimits.groupBy(county.state).mapValues(countList =>
            val oneUnit = countyList.map(county => county.oneUnitLimit)
            val twoUnit = countyList.map(county => county.twoUnitLimit)
            val threeUnit = countyList.map(county => county.threeUnitLimit)
            val fourUnit = countyList.map(county => county.fourUnitLimit)
            StateBoundries(
                oneUnitMax = oneUnit.max,
                oneUnitMin = oneUnit.min,
                twoUnitMax = twoUnit.max,
                twoUnitMin = twoUnit.min,
                threeUnitMax = threeUnit.max,
                threeUnitMin = threeUnit.min,
                fourUnitMax = fourUnit.max,
                fourUnitMin = fourUnit.min
            )
        )
    
    def assignLoanLimit(lar: LoanApplicationRegister): String {
        val loan = LoanLimitInfo(
            lar.property.totalUnits, 
            lar.loan.amount, 
            lar.lienStatus, 
            lar.geography.county,
            lar.geography.state
        )

        val conformingLoanLimit = match loan {
            case (loan.totalUnits >= 5) => "NA"
            case (loan.lienStatus == SecuredByFirstLien && loan.totalUnits == 1 and loan.amount <= 453100.00) => "C"
            case (loan.lienStatus == SecuredByFirstLien && loan.totalUnits == 2 and loan.amount <= 580150.00) => "C"
            case (loan.lienStatus == SecuredByFirstLien && loan.totalUnits == 3 and loan.amount <= 701250.00) => "C"
            case (loan.lienStatus == SecuredByFirstLien && loan.totalUnits == 4 and loan.amount <= 871450.00) => "C"
            case (loan.lienStatus == SecuredByFirstLien && loan.totalUnits == 1 and loan.amount > 721150.00) => "NC"
            case (loan.lienStatus == SecuredByFirstLien && loan.totalUnits == 2 and loan.amount > 923050.00) => "NC"
            case (loan.lienStatus == SecuredByFirstLien && loan.totalUnits == 3 and loan.amount > 1115800.00) => "NC"
            case (loan.lienStatus == SecuredByFirstLien && loan.totalUnits == 4 and loan.amount > 1386650.00) => "NC"
            case (loan.lienStatus == SecuredBySubordinateLien && loan.totalUnits == 1 and loan.amount <= 226550.00) => "C"
            case (loan.lienStatus == SecuredBySubordinateLien && loan.totalUnits == 2 and loan.amount <= 290075.00) => "C"
            case (loan.lienStatus == SecuredBySubordinateLien && loan.totalUnits == 3 and loan.amount <= 350625.00) => "C"
            case (loan.lienStatus == SecuredBySubordinateLien && loan.totalUnits == 4 and loan.amount <= 435725.00) => "C"
            case (loan.lienStatus == SecuredBySubordinateLien && loan.totalUnits == 1 and loan.amount > 360575.00) => "NC"
            case (loan.lienStatus == SecuredBySubordinateLien && loan.totalUnits == 2 and loan.amount > 461525.00) => "NC"
            case (loan.lienStatus == SecuredBySubordinateLien && loan.totalUnits == 3 and loan.amount > 557900.00) => "NC"
            case (loan.lienStatus == SecuredBySubordinateLien && loan.totalUnits == 4 and loan.amount > 693325.00) => "NC"
            case _ => "U"
        }

        if (conformingLoanLimit == "U" && loan.county != "NA") {
            val county = countyLoanLimitsByCounty.get(loan.state+loan.county)
            match loan {
                case (loan.lienStatus == SecuredByFirstLien && loan.totalUnits == 1 && loan.amount <= county.oneUnitLimit) => "C"
                case (loan.lienStatus == SecuredByFirstLien && loan.totalUnits == 1 && loan.amount <= county.oneUnitLimit) => "NC"
                case (loan.lienStatus == SecuredByFirstLien && loan.totalUnits == 2 && loan.amount <= county.twoUnitLimit) => "C"
                case (loan.lienStatus == SecuredByFirstLien && loan.totalUnits == 2 && loan.amount <= county.twoUnitLimit) => "NC"
                case (loan.lienStatus == SecuredByFirstLien && loan.totalUnits == 3 && loan.amount <= county.threeUnitLimit) => "C"
                case (loan.lienStatus == SecuredByFirstLien && loan.totalUnits == 3 && loan.amount <= county.threeUnitLimit) => "NC"
                case (loan.lienStatus == SecuredByFirstLien && loan.totalUnits == 4 && loan.amount <= county.fourUnitLimit) => "C"
                case (loan.lienStatus == SecuredByFirstLien && loan.totalUnits == 4 && loan.amount <= county.fourUnitLimit) => "NC"
                case (loan.lienStatus == SecuredBySubordinateLien && loan.totalUnits == 1 && loan.amount <= (county.oneUnitLimit)/2.0) => "C"
                case (loan.lienStatus == SecuredBySubordinateLien && loan.totalUnits == 1 && loan.amount <= (county.oneUnitLimit)/2.0) => "NC"
                case (loan.lienStatus == SecuredBySubordinateLien && loan.totalUnits == 2 && loan.amount <= (county.twoUnitLimit)/2.0) => "C"
                case (loan.lienStatus == SecuredBySubordinateLien && loan.totalUnits == 2 && loan.amount <= (county.twoUnitLimit)/2.0) => "NC"
                case (loan.lienStatus == SecuredBySubordinateLien && loan.totalUnits == 3 && loan.amount <= (county.threeUnitLimit)/2.0) => "C"
                case (loan.lienStatus == SecuredBySubordinateLien && loan.totalUnits == 3 && loan.amount <= (county.threeUnitLimit)/2.0) => "NC"
                case (loan.lienStatus == SecuredBySubordinateLien && loan.totalUnits == 4 && loan.amount <= (county.fourUnitLimit)/2.0) => "C"
                case (loan.lienStatus == SecuredBySubordinateLien && loan.totalUnits == 4 && loan.amount <= (county.fourUnitLimit)/2.0) => "NC"
            }
        } else if (conformingLoanLimit == "U" && loan.state != "NA") {
            val state:StateBoundries = countyLoanLimitsByState.get(loan.state)
            match loan {
                case (loan.lienStatus == SecuredByFirstLien && loan.totalUnits == 1 && loan.amount <= state.oneUnitMax) => "C"
                case (loan.lienStatus == SecuredByFirstLien && loan.totalUnits == 1 && loan.amount > state.oneUnitMin) => "NC"
                case (loan.lienStatus == SecuredByFirstLien && loan.totalUnits == 2 && loan.amount <= state.twoUnitMax) => "C"
                case (loan.lienStatus == SecuredByFirstLien && loan.totalUnits == 2 && loan.amount > state.twoUnitMin) => "NC"
                case (loan.lienStatus == SecuredByFirstLien && loan.totalUnits == 3 && loan.amount <= state.threeUnitMax) => "C"
                case (loan.lienStatus == SecuredByFirstLien && loan.totalUnits == 3 && loan.amount > state.threeUnitMin) => "NC"
                case (loan.lienStatus == SecuredByFirstLien && loan.totalUnits == 4 && loan.amount <= state.fourUnitMax) => "C"
                case (loan.lienStatus == SecuredByFirstLien && loan.totalUnits == 4 && loan.amount > state.fourUnitMin) => "NC"
                case (loan.lienStatus == SecuredBySubordinateLien && loan.totalUnits == 1 && loan.amount <= (state.oneUnitMax/2.0)) => "C"
                case (loan.lienStatus == SecuredBySubordinateLien && loan.totalUnits == 1 && loan.amount > (state.oneUnitMin/2.0)) => "NC"
                case (loan.lienStatus == SecuredBySubordinateLien && loan.totalUnits == 2 && loan.amount <= (state.twoUnitMax/2.0)) => "C"
                case (loan.lienStatus == SecuredBySubordinateLien && loan.totalUnits == 2 && loan.amount > (state.twoUnitMin/2.0)) => "NC"
                case (loan.lienStatus == SecuredBySubordinateLien && loan.totalUnits == 3 && loan.amount <= (state.threeUnitMax/2.0)) => "C"
                case (loan.lienStatus == SecuredBySubordinateLien && loan.totalUnits == 3 && loan.amount > (state.threeUnitMin/2.0)) => "NC"
                case (loan.lienStatus == SecuredBySubordinateLien && loan.totalUnits == 4 && loan.amount <= (state.fourUnitMax/2.0)) => "C"
                case (loan.lienStatus == SecuredBySubordinateLien && loan.totalUnits == 4 && loan.amount > (state.fourUnitMin/2.0)) => "NC"
            }
        } else {
            conformingLoanLimit
        }

        
    }
}