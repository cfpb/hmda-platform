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

  val fairfieldCountyTractList: List[String] = List(
    "09190043800","09120061100","09120061300","09190230502","09190230501","09140110601","09140110602","09190035102","09190035101","09190210502",
    "09190021701","09190021602","09190021502","09190211202","09190210202","09190210201","09190210102","09190210401","09190050302","09190021401",
    "09190021402","09190021601","09190020101","09190021702","09190021501","09190211201","09190210101","09190020102","09190210501","09190210402",
    "09190050301","09120257200","09190042600","09190042900","09120071000","09120071100","09120071200","09120071300","09120071400","09120071600",
    "09190043400","09190043500","09120071900","09120072000","09120072200","09190043600","09120072300","09120072400","09120072500","09120072600",
    "09120072700","09190043900","09190043700","09120072800","09190044000","09190044100","09190044200","09120073000","09120073200","09120073300",
    "09120073400","09120073500","09120073700","09120073800","09120074300","09120074400","09120080100","09190021801","09190021802","09120080200",
    "09120080500","09120080600","09120080700","09190044300","09190044400","09190044500","09190044600","09120090700","09140110100","09190200302",
    "09190210300","09120080900","09190022201","09190022202","09190022101","09190022102","09190020302","09190020301","09120090300","09190010101",
    "09190020600","09190021300","09190021100","09190035300","09120073600","09190050100","09120060500","09120100300","09190045300","09120061000",
    "09120080800","09190030300","09190210702","09190210701","09190010400","09190050400","09120060700","09120060800","09120060900","09190211300",
    "09190020200","09190020700","09190045101","09190050600","09120060600","09120072900","09190211100","09120074000","09140110301","09190211400",
    "09190220100","09190220300","09190245300","09190021900","09190022000","09120061200","09120061500","09120061600","09120070100","09190022400",
    "09190022300","09120070300","09120070400","09120070500","09120070600","09120070900","09120070200","09190205100","09190210900","09190042500",
    "09120105100","09120060400","09190050500","09190010102","09120060200","09190240200","09190205300","09120081000","09190220200","09190230400",
    "09190245400","09120100200","09120090100","09190055200","09190045400","09190035400","09190042800","09190245600","09190020400","09120090600",
    "09190011300","09190230300","09190021000","09190030500","09120073900","09120081300","09190010800","09190020500","09190020800","09190043300",
    "09120080400","09140110302","09190030200","09190045102","09190010900","09190011000","09120100100","09190205200","09190230100","09190257100",
    "09190210800","09190210600","09140110500","09140110400","09190043100","09190245200","09140110202","09190045200","09190035200","09120060100",
    "09190020900","09190200200","09190055100","09190200301","09120073100","09190211000","09120105200","09120060300","09120090500","09140110201",
    "09190030100","09120090200","09190010300","09190050200","09120081100","09190042700","09190240100","09190043200","09120090400","09120081200",
    "09190021200","09190230200","09190010201","09190010202","09190200100","09190245100","09190245500","09190011100","09190010500","09190010700",
    "09190010600","09190011200","09190043000","09190030400","09120061400","09120072100"
  )

  val fairfieldCountyId: String = "001"

  def assignLoanLimit(lar: LoanApplicationRegister,
                      year: Int,
                      overallLoanLimit: OverallLoanLimit,
                      countyLoanLimitsByCounty: Map[String, CountyLoanLimit],
                      countyLoanLimitsByState: Map[String, StateBoundries]): String = {
    
    val county = {
      if (year == 2025) {
        if (fairfieldCountyTractList.contains(lar.geography.tract)) fairfieldCountyId
        else lar.geography.county
      } else lar.geography.county
    }

    val loan = LoanLimitInfo(
      lar.property.totalUnits,
      lar.loan.amount,
      lar.lienStatus,
      county,
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
