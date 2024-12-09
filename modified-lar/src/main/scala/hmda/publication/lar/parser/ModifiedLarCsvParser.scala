package hmda.publication.lar.parser

import hmda.model.filing.lar._
import Math._

import com.typesafe.config.ConfigFactory
import hmda.model.modifiedlar.ModifiedLoanApplicationRegister
import hmda.parser.filing.lar.LarCsvParser
import hmda.model.census.CountyLoanLimit
import hmda.census.records.CountyLoanLimitRecords._
import hmda.parser.derivedFields._

import scala.math.BigDecimal.RoundingMode

object ModifiedLarCsvParser {
  val config = ConfigFactory.load()

  val countyLoanLimitFileName2018 =
      config.getString("hmda.countyLoanLimit.2018.fields.filename")
  val countyLoanLimitFileName2019 =
      config.getString("hmda.countyLoanLimit.2019.fields.filename")
  val countyLoanLimitFileName2020 =
      config.getString("hmda.countyLoanLimit.2020.fields.filename")
  val countyLoanLimitFileName2021 =
    config.getString("hmda.countyLoanLimit.2021.fields.filename")

  val countyLoanLimitFileName2022 =
    config.getString("hmda.countyLoanLimit.2022.fields.filename")

  val countyLoanLimitFileName2023 =
    config.getString("hmda.countyLoanLimit.2023.fields.filename")

  val countyLoanLimitFileName2024 =
    config.getString("hmda.countyLoanLimit.2024.fields.filename")

  val countyLoanLimitFileName2025 =
    config.getString("hmda.countyLoanLimit.2025.fields.filename")

  val countyLoanLimits2018: Seq[CountyLoanLimit] =
    parseCountyLoanLimitFile(countyLoanLimitFileName2018)
  val countyLoanLimits2019: Seq[CountyLoanLimit] =
    parseCountyLoanLimitFile(countyLoanLimitFileName2019)
  val countyLoanLimits2020: Seq[CountyLoanLimit] =
    parseCountyLoanLimitFile(countyLoanLimitFileName2020)
  val countyLoanLimits2021: Seq[CountyLoanLimit] =
    parseCountyLoanLimitFile(countyLoanLimitFileName2021)

  val countyLoanLimits2022: Seq[CountyLoanLimit] =
    parseCountyLoanLimitFile(countyLoanLimitFileName2022)

  val countyLoanLimits2023: Seq[CountyLoanLimit] =
    parseCountyLoanLimitFile(countyLoanLimitFileName2023)

  val countyLoanLimits2024: Seq[CountyLoanLimit] =
    parseCountyLoanLimitFile(countyLoanLimitFileName2024)

  val countyLoanLimits2025: Seq[CountyLoanLimit] =
    parseCountyLoanLimitFile(countyLoanLimitFileName2025)

  val overallLoanLimit2018 = overallLoanLimits(countyLoanLimits2018)
  val overallLoanLimit2019 = overallLoanLimits(countyLoanLimits2019)
  val overallLoanLimit2020 = overallLoanLimits(countyLoanLimits2020)
  val overallLoanLimit2021 = overallLoanLimits(countyLoanLimits2021)
  val overallLoanLimit2022 = overallLoanLimits(countyLoanLimits2022)
  val overallLoanLimit2023 = overallLoanLimits(countyLoanLimits2023)
  val overallLoanLimit2024 = overallLoanLimits(countyLoanLimits2024)
  val overallLoanLimit2025 = overallLoanLimits(countyLoanLimits2025)

  val countyLoanLimitsByCounty2018 = countyLoansLimitByCounty(countyLoanLimits2018)
  val countyLoanLimitsByCounty2019 = countyLoansLimitByCounty(countyLoanLimits2019)
  val countyLoanLimitsByCounty2020 = countyLoansLimitByCounty(countyLoanLimits2020)
  val countyLoanLimitsByCounty2021 = countyLoansLimitByCounty(countyLoanLimits2021)
  val countyLoanLimitsByCounty2022 = countyLoansLimitByCounty(countyLoanLimits2022)
  val countyLoanLimitsByCounty2023 = countyLoansLimitByCounty(countyLoanLimits2023)
  val countyLoanLimitsByCounty2024 = countyLoansLimitByCounty(countyLoanLimits2024)
  val countyLoanLimitsByCounty2025 = countyLoansLimitByCounty(countyLoanLimits2025)



  val countyLoanLimitsByState2018 = countyLoansLimitByState(countyLoanLimits2018)
  val countyLoanLimitsByState2019 = countyLoansLimitByState(countyLoanLimits2019)
  val countyLoanLimitsByState2020 = countyLoansLimitByState(countyLoanLimits2020)
  val countyLoanLimitsByState2021 = countyLoansLimitByState(countyLoanLimits2021)
  val countyLoanLimitsByState2022 = countyLoansLimitByState(countyLoanLimits2022)
  val countyLoanLimitsByState2023 = countyLoansLimitByState(countyLoanLimits2023)
  val countyLoanLimitsByState2024 = countyLoansLimitByState(countyLoanLimits2024)
  val countyLoanLimitsByState2025 = countyLoansLimitByState(countyLoanLimits2025)


  def apply(s: String, year: Int): ModifiedLoanApplicationRegister = {
    convert(LarCsvParser(s, true).getOrElse(LoanApplicationRegister()), year)
  }

  private def convert(
      lar: LoanApplicationRegister, year: Int): ModifiedLoanApplicationRegister = {
    val overallLoanLimit = getOverallLoanLimit(year)
    val countyLoanLimitsByCounty = getcountyLoanLimitsByCounty(year)
    val countyLoanLimitsByState = getcountyLoanLimitsByState(year)
    ModifiedLoanApplicationRegister(
      lar.larIdentifier.id,
      year,
      lar.loan.ULI,
      lar.larIdentifier.LEI,
      lar.loan.loanType.code,
      lar.loan.loanPurpose.code,
      lar.action.preapproval.code,
      lar.loan.constructionMethod.code,
      lar.loan.occupancy.code,
      roundToBigIntMidPoint(lar.loan.amount),
      lar.action.actionTakenType.code,
      lar.action.actionTakenDate,
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
                                          overallLoanLimit,
                                          countyLoanLimitsByCounty,
                                          countyLoanLimitsByState),
      EthnicityCategorization.assignEthnicityCategorization(lar),
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
    case x if x == "NA" || x == "Exempt"|| x==""  => x
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
      case x if x == "NA" || x == "Exempt" || x=="" => x
      case _ =>
        val percentage = (multifamilyUnits.toFloat / totalUnits.toFloat) * 100
        round(percentage).toString
    }

  private def convertPropertyValue(propertyValue: String): String =
    propertyValue match {
      case x if x == "NA" || x == "Exempt" || x=="" => x
      case x                               => roundToMidPoint(x.toDouble.toInt).toString
    }

  private def convertEmptyField(code: Int) =
    if (code == 0) "" else code.toString

  private def roundToMidPoint(x: Int): Int = {
    val rounded = 10000 * Math.floor(x / 10000) + 5000
    rounded.toDouble.toInt
  }

  private def roundToBigIntMidPoint(x: BigDecimal): String = {
    val rounded = 10000 * (x/10000).setScale(0, RoundingMode.FLOOR)  + 5000
    rounded.toBigInt.toString
  }

  private def getcountyLoanLimitsByCounty(year: Int) = {
    year match {
      case 2018 => countyLoanLimitsByCounty2018
      case 2019 => countyLoanLimitsByCounty2019
      case 2020 => countyLoanLimitsByCounty2020
      case 2021 => countyLoanLimitsByCounty2021
      case 2022 => countyLoanLimitsByCounty2022
      case 2023 => countyLoanLimitsByCounty2023
      case 2024 => countyLoanLimitsByCounty2024
      case 2025 => countyLoanLimitsByCounty2025



    }
  }

  private def getcountyLoanLimitsByState(year: Int) = {
    year match {
      case 2018 => countyLoanLimitsByState2018
      case 2019 => countyLoanLimitsByState2019
      case 2020 => countyLoanLimitsByState2020
      case 2021 => countyLoanLimitsByState2021
      case 2022 => countyLoanLimitsByState2022
      case 2023 => countyLoanLimitsByState2023
      case 2024 => countyLoanLimitsByState2024
      case 2025 => countyLoanLimitsByState2025



    }
  }

private def getOverallLoanLimit(year: Int) = {
    year match {
      case 2018 => overallLoanLimit2018
      case 2019 => overallLoanLimit2019
      case 2020 => overallLoanLimit2020
      case 2021 => overallLoanLimit2021
      case 2022 => overallLoanLimit2022
      case 2023 => overallLoanLimit2023
      case 2024 => overallLoanLimit2024
      case 2025 => overallLoanLimit2025


    }
  }

}
