package hmda.publication.lar.parser

import hmda.model.filing.lar._
import Math._

import hmda.model.modifiedlar.ModifiedLoanApplicationRegister
import hmda.parser.filing.lar.LarCsvParser
import hmda.publication.{ConformingLoanLimit, StateBoundries}
import hmda.model.census.CountyLoanLimit
import hmda.census.records.CensusRecords
import hmda.census.records.CountyLoanLimitRecords
import hmda.publication.lar._
import hmda.publication.EthnicityCategorization._

object ModifiedLarCsvParser {

  val censusRecords = CensusRecords.parseCensusFile
  val countyLoanLimits: Seq[CountyLoanLimit] =
    CountyLoanLimitRecords.parseCountyLoanLimitFile()
  val countyLoanLimitsByCounty: Map[String, CountyLoanLimit] =
    countyLoanLimits
      .map(county => county.stateCode + county.countyCode -> county)
      .toMap
  val countyLoanLimitsByState =
    countyLoanLimits.groupBy(county => county.stateAbbrv).mapValues {
      countyList =>
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
    }

  def apply(s: String): ModifiedLoanApplicationRegister = {
    convert(LarCsvParser(s, true).getOrElse(LoanApplicationRegister()))
  }

  private def convert(
      lar: LoanApplicationRegister): ModifiedLoanApplicationRegister = {
    ModifiedLoanApplicationRegister(
      lar.larIdentifier.id,
      lar.larIdentifier.LEI,
      lar.loan.loanType.code,
      lar.loan.loanPurpose.code,
      lar.action.preapproval.code,
      lar.loan.constructionMethod.code,
      lar.loan.occupancy.code,
      roundToMidPoint(lar.loan.amount.toInt),
      lar.action.actionTakenType.code,
      lar.geography.state,
      lar.geography.county,
      lar.geography.tract,
      convertEmptyField(lar.applicant.ethnicity.ethnicity1.code),
      convertEmptyField(lar.applicant.ethnicity.ethnicity2.code),
      convertEmptyField(lar.applicant.ethnicity.ethnicity3.code),
      convertEmptyField(lar.applicant.ethnicity.ethnicity4.code),
      convertEmptyField(lar.applicant.ethnicity.ethnicity5.code),
      lar.applicant.ethnicity.ethnicityObserved.code,
      convertEmptyField(lar.coApplicant.ethnicity.ethnicity1.code),
      convertEmptyField(lar.coApplicant.ethnicity.ethnicity2.code),
      convertEmptyField(lar.coApplicant.ethnicity.ethnicity3.code),
      convertEmptyField(lar.coApplicant.ethnicity.ethnicity4.code),
      convertEmptyField(lar.coApplicant.ethnicity.ethnicity5.code),
      lar.coApplicant.ethnicity.ethnicityObserved.code,
      convertEmptyField(lar.applicant.race.race1.code),
      convertEmptyField(lar.applicant.race.race2.code),
      convertEmptyField(lar.applicant.race.race3.code),
      convertEmptyField(lar.applicant.race.race4.code),
      convertEmptyField(lar.applicant.race.race5.code),
      convertEmptyField(lar.coApplicant.race.race1.code),
      convertEmptyField(lar.coApplicant.race.race2.code),
      convertEmptyField(lar.coApplicant.race.race3.code),
      convertEmptyField(lar.coApplicant.race.race4.code),
      convertEmptyField(lar.coApplicant.race.race5.code),
      lar.applicant.race.raceObserved.code,
      lar.coApplicant.race.raceObserved.code,
      lar.applicant.sex.sexEnum.code,
      lar.coApplicant.sex.sexEnum.code,
      lar.applicant.sex.sexObservedEnum.code,
      lar.coApplicant.sex.sexObservedEnum.code,
      convertAge(lar.applicant.age),
      isAgeGreaterThan62(lar.applicant.age),
      convertAge(lar.coApplicant.age),
      isAgeGreaterThan62(lar.coApplicant.age),
      lar.income,
      lar.purchaserType.code,
      lar.loan.rateSpread,
      lar.hoepaStatus.code,
      lar.lienStatus.code,
      lar.applicant.creditScoreType.code,
      lar.coApplicant.creditScoreType.code,
      convertEmptyField(lar.denial.denialReason1.code),
      convertEmptyField(lar.denial.denialReason2.code),
      convertEmptyField(lar.denial.denialReason3.code),
      convertEmptyField(lar.denial.denialReason4.code),
      lar.loanDisclosure.totalLoanCosts,
      lar.loanDisclosure.totalPointsAndFees,
      lar.loanDisclosure.originationCharges,
      lar.loanDisclosure.discountPoints,
      lar.loanDisclosure.lenderCredits,
      lar.loan.interestRate,
      lar.loan.prepaymentPenaltyTerm,
      converDebtToIncomeRatio(lar.loan.debtToIncomeRatio),
      lar.loan.combinedLoanToValueRatio,
      lar.loan.loanTerm,
      lar.loan.introductoryRatePeriod,
      lar.nonAmortizingFeatures.balloonPayment.code,
      lar.nonAmortizingFeatures.interestOnlyPayments.code,
      lar.nonAmortizingFeatures.negativeAmortization.code,
      lar.nonAmortizingFeatures.otherNonAmortizingFeatures.code,
      convertPropertyValue(lar.property.propertyValue),
      lar.property.manufacturedHomeSecuredProperty.code,
      lar.property.manufacturedHomeLandPropertyInterest.code,
      convertTotalUnits(lar.property.totalUnits),
      convertMultifamilyAffordableUnits(lar.property.multiFamilyAffordableUnits,
                                        lar.property.totalUnits),
      lar.applicationSubmission.code,
      lar.payableToInstitution.code,
      convertEmptyField(lar.AUS.aus1.code),
      convertEmptyField(lar.AUS.aus2.code),
      convertEmptyField(lar.AUS.aus3.code),
      convertEmptyField(lar.AUS.aus4.code),
      convertEmptyField(lar.AUS.aus5.code),
      lar.reverseMortgage.code,
      lar.lineOfCredit.code,
      lar.businessOrCommercialPurpose.code,
      ConformingLoanLimit.assignLoanLimit(lar,
                                          countyLoanLimitsByCounty,
                                          countyLoanLimitsByState),
      assignEthnicityCategorization(lar),
      RaceCategorization.assignRaceCategorization(lar),
      SexCategorization.assignSexCategorization(lar),
      DwellingCategorization.assignDwellingCategorization(lar),
      LoanProductTypeCategorization.assignLoanProductTypeCategorization(lar)
    )
  }

  private def convertAge(age: Int): String = age match {
    case x if x == 8888             => "8888"
    case x if x == 9999             => "9999"
    case x if 0 until 25 contains x => "<25"
    case x if 25 to 34 contains x   => "25-34"
    case x if 35 to 44 contains x   => "35-44"
    case x if 45 to 54 contains x   => "45-54"
    case x if 55 to 64 contains x   => "55-64"
    case x if 65 to 74 contains x   => "65-74"
    case x if x > 74                => ">74"
  }

  private def isAgeGreaterThan62(age: Int): String = age match {
    case x if x == 8888 || x == 9999 => "NA"
    case x if x >= 62                => "Yes"
    case x if x < 62                 => "No"
  }

  private def converDebtToIncomeRatio(ratio: String): String = ratio match {
    case x if x == "NA" || x == "Exempt" => x
    case _ =>
      ratio.toDouble.toInt match {
        case x if x < 20                 => "<20%"
        case x if 20 until 30 contains x => "20%-<30%"
        case x if 30 until 36 contains x => "30%-<36%"
        case x if 36 until 50 contains x => x.toString
        case x if 50 to 60 contains x    => "50%-60%"
        case x if x >= 60                => ">60%"
      }
  }

  private def convertTotalUnits(totalUnits: Int): String = totalUnits match {
    case x if x < 5                 => x.toString
    case x if 5 to 24 contains x    => "5-24"
    case x if 25 to 49 contains x   => "25-49"
    case x if 50 to 99 contains x   => "50-99"
    case x if 100 to 149 contains x => "100-149"
    case x if x >= 150              => ">149"
  }

  private def convertMultifamilyAffordableUnits(multifamilyUnits: String,
                                                totalUnits: Int): String =
    multifamilyUnits match {
      case x if x == "NA" || x == "Exempt" => x
      case _ =>
        val percentage = (multifamilyUnits.toFloat / totalUnits.toFloat) * 100
        round(percentage).toString
    }

  private def convertPropertyValue(propertyValue: String): String =
    propertyValue match {
      case x if x == "NA" || x == "Exempt" => x
      case x                               => roundToMidPoint(x.toDouble.toInt).toString
    }

  private def convertEmptyField(code: Int) =
    if (code == 0) "" else code.toString

  private def roundToMidPoint(x: Int): Int = {
    val rounded = 10000 * Math.floor(x / 10000) + 5000
    rounded.toDouble.toInt
  }

}
