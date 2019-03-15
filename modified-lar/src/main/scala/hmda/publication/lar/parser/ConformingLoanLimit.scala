package hmda.publication

import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.filing.lar.enums._
import hmda.model.census.CountyLoanLimit

case class LoanLimitInfo(
    totalUnits: Int,
    amount: Double,
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

  def assignLoanLimit(
      lar: LoanApplicationRegister,
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
      else if (loan.lienStatus == SecuredByFirstLien && loan.totalUnits == 1 && loan.amount <= 453100.00)
        "C"
      else if (loan.lienStatus == SecuredByFirstLien && loan.totalUnits == 2 && loan.amount <= 580150.00)
        "C"
      else if (loan.lienStatus == SecuredByFirstLien && loan.totalUnits == 3 && loan.amount <= 701250.00)
        "C"
      else if (loan.lienStatus == SecuredByFirstLien && loan.totalUnits == 4 && loan.amount <= 871450.00)
        "C"
      else if (loan.lienStatus == SecuredByFirstLien && loan.totalUnits == 1 && loan.amount > 721150.00)
        "NC"
      else if (loan.lienStatus == SecuredByFirstLien && loan.totalUnits == 2 && loan.amount > 923050.00)
        "NC"
      else if (loan.lienStatus == SecuredByFirstLien && loan.totalUnits == 3 && loan.amount > 1115800.00)
        "NC"
      else if (loan.lienStatus == SecuredByFirstLien && loan.totalUnits == 4 && loan.amount > 1386650.00)
        "NC"
      else if (loan.lienStatus == SecuredBySubordinateLien && loan.totalUnits == 1 && loan.amount <= 226550.00)
        "C"
      else if (loan.lienStatus == SecuredBySubordinateLien && loan.totalUnits == 2 && loan.amount <= 290075.00)
        "C"
      else if (loan.lienStatus == SecuredBySubordinateLien && loan.totalUnits == 3 && loan.amount <= 350625.00)
        "C"
      else if (loan.lienStatus == SecuredBySubordinateLien && loan.totalUnits == 4 && loan.amount <= 435725.00)
        "C"
      else if (loan.lienStatus == SecuredBySubordinateLien && loan.totalUnits == 1 && loan.amount > 360575.00)
        "NC"
      else if (loan.lienStatus == SecuredBySubordinateLien && loan.totalUnits == 2 && loan.amount > 461525.00)
        "NC"
      else if (loan.lienStatus == SecuredBySubordinateLien && loan.totalUnits == 3 && loan.amount > 557900.00)
        "NC"
      else if (loan.lienStatus == SecuredBySubordinateLien && loan.totalUnits == 4 && loan.amount > 693325.00)
        "NC"
      else "U"
    }

    if (conformingLoanLimit == "U" && loan.county != "NA") {
      countyLoanLimitsByCounty.get(loan.state + loan.county) match {
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
          if (loan.lienStatus == SecuredByFirstLien && loan.totalUnits == 1 && loan.amount <= state.oneUnitMax)
            "C"
          else if (loan.lienStatus == SecuredByFirstLien && loan.totalUnits == 1 && loan.amount > state.oneUnitMin)
            "NC"
          else if (loan.lienStatus == SecuredByFirstLien && loan.totalUnits == 2 && loan.amount <= state.twoUnitMax)
            "C"
          else if (loan.lienStatus == SecuredByFirstLien && loan.totalUnits == 2 && loan.amount > state.twoUnitMin)
            "NC"
          else if (loan.lienStatus == SecuredByFirstLien && loan.totalUnits == 3 && loan.amount <= state.threeUnitMax)
            "C"
          else if (loan.lienStatus == SecuredByFirstLien && loan.totalUnits == 3 && loan.amount > state.threeUnitMin)
            "NC"
          else if (loan.lienStatus == SecuredByFirstLien && loan.totalUnits == 4 && loan.amount <= state.fourUnitMax)
            "C"
          else if (loan.lienStatus == SecuredByFirstLien && loan.totalUnits == 4 && loan.amount > state.fourUnitMin)
            "NC"
          else if (loan.lienStatus == SecuredBySubordinateLien && loan.totalUnits == 1 && loan.amount <= (state.oneUnitMax / 2.0))
            "C"
          else if (loan.lienStatus == SecuredBySubordinateLien && loan.totalUnits == 1 && loan.amount > (state.oneUnitMin / 2.0))
            "NC"
          else if (loan.lienStatus == SecuredBySubordinateLien && loan.totalUnits == 2 && loan.amount <= (state.twoUnitMax / 2.0))
            "C"
          else if (loan.lienStatus == SecuredBySubordinateLien && loan.totalUnits == 2 && loan.amount > (state.twoUnitMin / 2.0))
            "NC"
          else if (loan.lienStatus == SecuredBySubordinateLien && loan.totalUnits == 3 && loan.amount <= (state.threeUnitMax / 2.0))
            "C"
          else if (loan.lienStatus == SecuredBySubordinateLien && loan.totalUnits == 3 && loan.amount > (state.threeUnitMin / 2.0))
            "NC"
          else if (loan.lienStatus == SecuredBySubordinateLien && loan.totalUnits == 4 && loan.amount <= (state.fourUnitMax / 2.0))
            "C"
          else if (loan.lienStatus == SecuredBySubordinateLien && loan.totalUnits == 4 && loan.amount > (state.fourUnitMin / 2.0))
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
