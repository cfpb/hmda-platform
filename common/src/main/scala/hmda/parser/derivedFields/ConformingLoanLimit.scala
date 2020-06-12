package hmda.parser.derivedFields

import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.filing.lar.enums._
import hmda.model.census.CountyLoanLimit
import hmda.census.records.OverallLoanLimit
// $COVERAGE-OFF$
case class LoanLimitInfo(
  totalUnits: Int,
  amount: BigDecimal,
  lienStatus: LienStatusEnum,
  county: String,
  state: String
)

case class StateBoundries(
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

  def assignLoanLimit(lar: LoanApplicationRegister,
                      overallLoanLimit: OverallLoanLimit,
                      countyLoanLimitsByCounty: Map[String, CountyLoanLimit],
                      countyLoanLimitsByState: Map[String, StateBoundries]): String = {
    val loan = LoanLimitInfo(
      lar.property.totalUnits,
      lar.loan.amount,
      lar.lienStatus,
      lar.geography.county,
      lar.geography.state
    )

    val conformingLoanLimit = {
      if (loan.totalUnits > 4) "NA"
      else if (loan.lienStatus == SecuredByFirstLien && loan.totalUnits == 1 && loan.amount <= overallLoanLimit.oneUnitMin)
        "C"
      else if (loan.lienStatus == SecuredByFirstLien && loan.totalUnits == 2 && loan.amount <= overallLoanLimit.twoUnitMin)
        "C"
      else if (loan.lienStatus == SecuredByFirstLien && loan.totalUnits == 3 && loan.amount <= overallLoanLimit.threeUnitMin)
        "C"
      else if (loan.lienStatus == SecuredByFirstLien && loan.totalUnits == 4 && loan.amount <= overallLoanLimit.fourUnitMin)
        "C"
      else if (loan.lienStatus == SecuredByFirstLien && loan.totalUnits == 1 && loan.amount > overallLoanLimit.oneUnitMax)
        "NC"
      else if (loan.lienStatus == SecuredByFirstLien && loan.totalUnits == 2 && loan.amount > overallLoanLimit.twoUnitMax)
        "NC"
      else if (loan.lienStatus == SecuredByFirstLien && loan.totalUnits == 3 && loan.amount > overallLoanLimit.threeUnitMax)
        "NC"
      else if (loan.lienStatus == SecuredByFirstLien && loan.totalUnits == 4 && loan.amount > overallLoanLimit.fourUnitMax)
        "NC"
      else if (loan.lienStatus == SecuredBySubordinateLien && loan.totalUnits == 1 && loan.amount <= overallLoanLimit.oneUnitMin / 2.0)
        "C"
      else if (loan.lienStatus == SecuredBySubordinateLien && loan.totalUnits == 2 && loan.amount <= overallLoanLimit.twoUnitMin / 2.0)
        "C"
      else if (loan.lienStatus == SecuredBySubordinateLien && loan.totalUnits == 3 && loan.amount <= overallLoanLimit.threeUnitMin / 2.0)
        "C"
      else if (loan.lienStatus == SecuredBySubordinateLien && loan.totalUnits == 4 && loan.amount <= overallLoanLimit.fourUnitMin / 2.0)
        "C"
      else if (loan.lienStatus == SecuredBySubordinateLien && loan.totalUnits == 1 && loan.amount > overallLoanLimit.oneUnitMax / 2.0)
        "NC"
      else if (loan.lienStatus == SecuredBySubordinateLien && loan.totalUnits == 2 && loan.amount > overallLoanLimit.twoUnitMax / 2.0)
        "NC"
      else if (loan.lienStatus == SecuredBySubordinateLien && loan.totalUnits == 3 && loan.amount > overallLoanLimit.threeUnitMax / 2.0)
        "NC"
      else if (loan.lienStatus == SecuredBySubordinateLien && loan.totalUnits == 4 && loan.amount > overallLoanLimit.fourUnitMax / 2.0)
        "NC"
      else "U"
    }

    if (conformingLoanLimit == "U" && loan.county != "NA") {
      countyLoanLimitsByCounty.get(loan.county) match {
        case Some(county) => {
          if (loan.lienStatus == SecuredByFirstLien && loan.totalUnits == 1 && loan.amount <= county.oneUnitLimit)
            "C"
          else if (loan.lienStatus == SecuredByFirstLien && loan.totalUnits == 1 && loan.amount > county.oneUnitLimit)
            "NC"
          else if (loan.lienStatus == SecuredByFirstLien && loan.totalUnits == 2 && loan.amount <= county.twoUnitLimit)
            "C"
          else if (loan.lienStatus == SecuredByFirstLien && loan.totalUnits == 2 && loan.amount > county.twoUnitLimit)
            "NC"
          else if (loan.lienStatus == SecuredByFirstLien && loan.totalUnits == 3 && loan.amount <= county.threeUnitLimit)
            "C"
          else if (loan.lienStatus == SecuredByFirstLien && loan.totalUnits == 3 && loan.amount > county.threeUnitLimit)
            "NC"
          else if (loan.lienStatus == SecuredByFirstLien && loan.totalUnits == 4 && loan.amount <= county.fourUnitLimit)
            "C"
          else if (loan.lienStatus == SecuredByFirstLien && loan.totalUnits == 4 && loan.amount > county.fourUnitLimit)
            "NC"
          else if (loan.lienStatus == SecuredBySubordinateLien && loan.totalUnits == 1 && loan.amount <= (county.oneUnitLimit) / 2.0)
            "C"
          else if (loan.lienStatus == SecuredBySubordinateLien && loan.totalUnits == 1 && loan.amount > (county.oneUnitLimit) / 2.0)
            "NC"
          else if (loan.lienStatus == SecuredBySubordinateLien && loan.totalUnits == 2 && loan.amount <= (county.twoUnitLimit) / 2.0)
            "C"
          else if (loan.lienStatus == SecuredBySubordinateLien && loan.totalUnits == 2 && loan.amount > (county.twoUnitLimit) / 2.0)
            "NC"
          else if (loan.lienStatus == SecuredBySubordinateLien && loan.totalUnits == 3 && loan.amount <= (county.threeUnitLimit) / 2.0)
            "C"
          else if (loan.lienStatus == SecuredBySubordinateLien && loan.totalUnits == 3 && loan.amount > (county.threeUnitLimit) / 2.0)
            "NC"
          else if (loan.lienStatus == SecuredBySubordinateLien && loan.totalUnits == 4 && loan.amount <= (county.fourUnitLimit) / 2.0)
            "C"
          else if (loan.lienStatus == SecuredBySubordinateLien && loan.totalUnits == 4 && loan.amount > (county.fourUnitLimit) / 2.0)
            "NC"
          else "U"
        }
        case None => "U"
      }
    } else if (conformingLoanLimit == "U" && loan.state != "NA") {
      countyLoanLimitsByState.get(loan.state) match {
        case Some(state) => {
          if (loan.lienStatus == SecuredByFirstLien && loan.totalUnits == 1 && loan.amount <= state.oneUnitMin)
            "C"
          else if (loan.lienStatus == SecuredByFirstLien && loan.totalUnits == 1 && loan.amount > state.oneUnitMax)
            "NC"
          else if (loan.lienStatus == SecuredByFirstLien && loan.totalUnits == 2 && loan.amount <= state.twoUnitMin)
            "C"
          else if (loan.lienStatus == SecuredByFirstLien && loan.totalUnits == 2 && loan.amount > state.twoUnitMax)
            "NC"
          else if (loan.lienStatus == SecuredByFirstLien && loan.totalUnits == 3 && loan.amount <= state.threeUnitMin)
            "C"
          else if (loan.lienStatus == SecuredByFirstLien && loan.totalUnits == 3 && loan.amount > state.threeUnitMax)
            "NC"
          else if (loan.lienStatus == SecuredByFirstLien && loan.totalUnits == 4 && loan.amount <= state.fourUnitMin)
            "C"
          else if (loan.lienStatus == SecuredByFirstLien && loan.totalUnits == 4 && loan.amount > state.fourUnitMax)
            "NC"
          else if (loan.lienStatus == SecuredBySubordinateLien && loan.totalUnits == 1 && loan.amount <= (state.oneUnitMin / 2.0))
            "C"
          else if (loan.lienStatus == SecuredBySubordinateLien && loan.totalUnits == 1 && loan.amount > (state.oneUnitMax / 2.0))
            "NC"
          else if (loan.lienStatus == SecuredBySubordinateLien && loan.totalUnits == 2 && loan.amount <= (state.twoUnitMin / 2.0))
            "C"
          else if (loan.lienStatus == SecuredBySubordinateLien && loan.totalUnits == 2 && loan.amount > (state.twoUnitMax / 2.0))
            "NC"
          else if (loan.lienStatus == SecuredBySubordinateLien && loan.totalUnits == 3 && loan.amount <= (state.threeUnitMin / 2.0))
            "C"
          else if (loan.lienStatus == SecuredBySubordinateLien && loan.totalUnits == 3 && loan.amount > (state.threeUnitMax / 2.0))
            "NC"
          else if (loan.lienStatus == SecuredBySubordinateLien && loan.totalUnits == 4 && loan.amount <= (state.fourUnitMin / 2.0))
            "C"
          else if (loan.lienStatus == SecuredBySubordinateLien && loan.totalUnits == 4 && loan.amount > (state.fourUnitMax / 2.0))
            "NC"
          else "U"
        }
        case None => "U"
      }

    } else {
      conformingLoanLimit
    }

  }
}
// $COVERAGE-ON$
