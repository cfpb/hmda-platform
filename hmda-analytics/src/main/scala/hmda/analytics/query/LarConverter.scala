package hmda.analytics.query

import hmda.model.filing.lar.LoanApplicationRegister
import hmda.parser.derivedFields._
import hmda.model.census.CountyLoanLimit
import hmda.census.records.CountyLoanLimitRecords._
import hmda.census.records.CensusRecords._
import com.typesafe.config.ConfigFactory
import java.security.MessageDigest

import hmda.util.conversion.LarStringFormatter

object LarConverter {

  val config = ConfigFactory.load()

  val censusFileName2019 =
    config.getString("hmda.census.fields.2019.filename")

  val censusFileName2020 =
    config.getString("hmda.census.fields.2020.filename")

  val censusFileName2021 =
    config.getString("hmda.census.fields.2021.filename")

  val censusFileName2022 =
    config.getString("hmda.census.fields.2022.filename")

  val censusFileName2023 =
    config.getString("hmda.census.fields.2023.filename")

  val censusFileName2024 =
    config.getString("hmda.census.fields.2024.filename")

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

  val overallLoanLimit2018 = overallLoanLimits(countyLoanLimits2018)
  val overallLoanLimit2019 = overallLoanLimits(countyLoanLimits2019)
  val overallLoanLimit2020 = overallLoanLimits(countyLoanLimits2020)
  val overallLoanLimit2021 = overallLoanLimits(countyLoanLimits2021)
  val overallLoanLimit2022 = overallLoanLimits(countyLoanLimits2022)
  val overallLoanLimit2023 = overallLoanLimits(countyLoanLimits2023)
  val overallLoanLimit2024 = overallLoanLimits(countyLoanLimits2024)


  val countyLoanLimitsByCounty2018 = countyLoansLimitByCounty(countyLoanLimits2018)
  val countyLoanLimitsByCounty2019 = countyLoansLimitByCounty(countyLoanLimits2019)
  val countyLoanLimitsByCounty2020 = countyLoansLimitByCounty(countyLoanLimits2020)
  val countyLoanLimitsByCounty2021 = countyLoansLimitByCounty(countyLoanLimits2021)
  val countyLoanLimitsByCounty2022 = countyLoansLimitByCounty(countyLoanLimits2022)
  val countyLoanLimitsByCounty2023 = countyLoansLimitByCounty(countyLoanLimits2023)
  val countyLoanLimitsByCounty2024 = countyLoansLimitByCounty(countyLoanLimits2024)

  val countyLoanLimitsByState2018 = countyLoansLimitByState(countyLoanLimits2018)
  val countyLoanLimitsByState2019 = countyLoansLimitByState(countyLoanLimits2019)
  val countyLoanLimitsByState2020 = countyLoansLimitByState(countyLoanLimits2020)
  val countyLoanLimitsByState2021 = countyLoansLimitByState(countyLoanLimits2021)
  val countyLoanLimitsByState2022 = countyLoansLimitByState(countyLoanLimits2022)
  val countyLoanLimitsByState2023 = countyLoansLimitByState(countyLoanLimits2023)
  val countyLoanLimitsByState2024 = countyLoansLimitByState(countyLoanLimits2024)

  def apply(
    lar: LoanApplicationRegister,
    year: Int,
    isQuarterly: Boolean = false
  ): LarEntity = {
    val census = getCensusOnTractandCounty(lar.geography.tract, lar.geography.county, year)
    val overallLoanLimit = getOverallLoanLimit(year)
    val countyLoanLimitsByCounty = getcountyLoanLimitsByCounty(year)
    val countyLoanLimitsByState = getcountyLoanLimitsByState(year)
    val checksum = MessageDigest.getInstance("MD5")
      .digest(LarStringFormatter.larString(lar).toUpperCase().getBytes())
      .map(0xFF & _)
      .map { "%02x".format(_) }.foldLeft(""){_ + _}
    LarEntity(
      lar.larIdentifier.id,
      lar.larIdentifier.LEI,
      lar.loan.ULI,
      lar.loan.applicationDate,
      lar.loan.loanType.code,
      lar.loan.loanPurpose.code,
      lar.action.preapproval.code,
      lar.loan.constructionMethod.code,
      lar.loan.occupancy.code,
      lar.loan.amount,
      lar.action.actionTakenType.code,
      lar.action.actionTakenDate,
      lar.geography.street,
      lar.geography.city,
      lar.geography.state,
      lar.geography.zipCode,
      lar.geography.county,
      lar.geography.tract,
      convertEmptyField(lar.applicant.ethnicity.ethnicity1.code),
      convertEmptyField(lar.applicant.ethnicity.ethnicity2.code),
      convertEmptyField(lar.applicant.ethnicity.ethnicity3.code),
      convertEmptyField(lar.applicant.ethnicity.ethnicity4.code),
      convertEmptyField(lar.applicant.ethnicity.ethnicity5.code),
      lar.applicant.ethnicity.otherHispanicOrLatino,
      convertEmptyField(lar.coApplicant.ethnicity.ethnicity1.code),
      convertEmptyField(lar.coApplicant.ethnicity.ethnicity2.code),
      convertEmptyField(lar.coApplicant.ethnicity.ethnicity3.code),
      convertEmptyField(lar.coApplicant.ethnicity.ethnicity4.code),
      convertEmptyField(lar.coApplicant.ethnicity.ethnicity5.code),
      lar.coApplicant.ethnicity.otherHispanicOrLatino,
      lar.applicant.ethnicity.ethnicityObserved.code,
      lar.coApplicant.ethnicity.ethnicityObserved.code,
      convertEmptyField(lar.applicant.race.race1.code),
      convertEmptyField(lar.applicant.race.race2.code),
      convertEmptyField(lar.applicant.race.race3.code),
      convertEmptyField(lar.applicant.race.race4.code),
      convertEmptyField(lar.applicant.race.race5.code),
      lar.applicant.race.otherNativeRace,
      lar.applicant.race.otherAsianRace,
      lar.applicant.race.otherPacificIslanderRace,
      convertEmptyField(lar.coApplicant.race.race1.code),
      convertEmptyField(lar.coApplicant.race.race2.code),
      convertEmptyField(lar.coApplicant.race.race3.code),
      convertEmptyField(lar.coApplicant.race.race4.code),
      convertEmptyField(lar.coApplicant.race.race5.code),
      lar.coApplicant.race.otherNativeRace,
      lar.coApplicant.race.otherAsianRace,
      lar.coApplicant.race.otherPacificIslanderRace,
      lar.applicant.race.raceObserved.code,
      lar.coApplicant.race.raceObserved.code,
      lar.applicant.sex.sexEnum.code,
      lar.coApplicant.sex.sexEnum.code,
      lar.applicant.sex.sexObservedEnum.code,
      lar.coApplicant.sex.sexObservedEnum.code,
      lar.applicant.age,
      lar.coApplicant.age,
      lar.income,
      lar.purchaserType.code,
      lar.loan.rateSpread,
      lar.hoepaStatus.code,
      lar.lienStatus.code,
      lar.applicant.creditScore,
      lar.coApplicant.creditScore,
      lar.applicant.creditScoreType.code,
      lar.applicant.otherCreditScoreModel,
      lar.coApplicant.creditScoreType.code,
      lar.coApplicant.otherCreditScoreModel,
      convertEmptyField(lar.denial.denialReason1.code),
      convertEmptyField(lar.denial.denialReason2.code),
      convertEmptyField(lar.denial.denialReason3.code),
      convertEmptyField(lar.denial.denialReason4.code),
      lar.denial.otherDenialReason,
      lar.loanDisclosure.totalLoanCosts,
      lar.loanDisclosure.totalPointsAndFees,
      lar.loanDisclosure.originationCharges,
      lar.loanDisclosure.discountPoints,
      lar.loanDisclosure.lenderCredits,
      lar.loan.interestRate,
      lar.loan.prepaymentPenaltyTerm,
      lar.loan.debtToIncomeRatio,
      lar.loan.combinedLoanToValueRatio,
      lar.loan.loanTerm,
      lar.loan.introductoryRatePeriod,
      lar.nonAmortizingFeatures.balloonPayment.code,
      lar.nonAmortizingFeatures.interestOnlyPayments.code,
      lar.nonAmortizingFeatures.negativeAmortization.code,
      lar.nonAmortizingFeatures.otherNonAmortizingFeatures.code,
      lar.property.propertyValue,
      lar.property.manufacturedHomeSecuredProperty.code,
      lar.property.manufacturedHomeLandPropertyInterest.code,
      lar.property.totalUnits,
      lar.property.multiFamilyAffordableUnits,
      lar.applicationSubmission.code,
      lar.payableToInstitution.code,
      lar.larIdentifier.NMLSRIdentifier,
      convertEmptyField(lar.AUS.aus1.code),
      convertEmptyField(lar.AUS.aus2.code),
      convertEmptyField(lar.AUS.aus3.code),
      convertEmptyField(lar.AUS.aus4.code),
      convertEmptyField(lar.AUS.aus5.code),
      lar.AUS.otherAUS,
      lar.ausResult.ausResult1.code,
      convertEmptyField(lar.ausResult.ausResult2.code),
      convertEmptyField(lar.ausResult.ausResult3.code),
      convertEmptyField(lar.ausResult.ausResult4.code),
      convertEmptyField(lar.ausResult.ausResult5.code),
      lar.ausResult.otherAusResult,
      lar.reverseMortgage.code,
      lar.lineOfCredit.code,
      lar.businessOrCommercialPurpose.code,
      ConformingLoanLimit.assignLoanLimit(lar, overallLoanLimit, countyLoanLimitsByCounty, countyLoanLimitsByState),
      EthnicityCategorization.assignEthnicityCategorization(lar),
      RaceCategorization.assignRaceCategorization(lar),
      SexCategorization.assignSexCategorization(lar),
      DwellingCategorization.assignDwellingCategorization(lar),
      LoanProductTypeCategorization.assignLoanProductTypeCategorization(lar),
      census.population,
      census.minorityPopulationPercent,
      census.medianIncome,
      census.occupiedUnits,
      census.oneToFourFamilyUnits,
      census.medianAge,
      census.tracttoMsaIncomePercent,
      isQuarterly,
      census.msaMd.toString,
      census.name,
      checksum
    )
  }

  private def convertEmptyField(code: Int) =
    if (code == 0) "" else code.toString

  private def getcountyLoanLimitsByCounty(year: Int) = {
    year match {
      case 2018 => countyLoanLimitsByCounty2018
      case 2019 => countyLoanLimitsByCounty2019
      case 2020 => countyLoanLimitsByCounty2020
      case 2021 => countyLoanLimitsByCounty2021
      case 2022 => countyLoanLimitsByCounty2022
      case 2023 => countyLoanLimitsByCounty2023
      case 2024 => countyLoanLimitsByCounty2024
      case 2025 => countyLoanLimitsByCounty2024
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
      case 2025 => countyLoanLimitsByState2024
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
      case 2025 => overallLoanLimit2024
    }
  }

}
